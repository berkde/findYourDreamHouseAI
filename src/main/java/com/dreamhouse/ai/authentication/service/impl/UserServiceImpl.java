package com.dreamhouse.ai.authentication.service.impl;

import com.dreamhouse.ai.authentication.dto.UserDTO;
import com.dreamhouse.ai.authentication.exception.*;
import com.dreamhouse.ai.mapper.UserMapper;
import com.dreamhouse.ai.authentication.model.entity.AddressEntity;
import com.dreamhouse.ai.authentication.model.entity.AuthorityEntity;
import com.dreamhouse.ai.authentication.model.entity.RoleEntity;
import com.dreamhouse.ai.authentication.model.entity.UserEntity;
import com.dreamhouse.ai.authentication.model.request.AddressCreationRequestModel;
import com.dreamhouse.ai.authentication.model.request.RoleAuthorityEditRequestModel;
import com.dreamhouse.ai.authentication.model.request.UserRegisterRequest;
import com.dreamhouse.ai.authentication.model.response.UserRegisterResponse;
import com.dreamhouse.ai.authentication.model.security.User;
import com.dreamhouse.ai.authentication.repository.AuthorityRepository;
import com.dreamhouse.ai.authentication.repository.RoleRepository;
import com.dreamhouse.ai.authentication.repository.UserRepository;
import com.dreamhouse.ai.authentication.service.UserService;
import com.dreamhouse.ai.cache.service.impl.QueryKeyServiceImpl;
import com.dreamhouse.ai.house.model.entity.HouseAdImageEntity;
import com.dreamhouse.ai.listener.event.ImageDeleteBatchEvent;
import org.hibernate.exception.LockAcquisitionException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toSet;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RedissonClient redissonClient;
    private final QueryKeyServiceImpl queryKeyService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           AuthorityRepository authorityRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           RedissonClient redissonClient,
                           QueryKeyServiceImpl queryKeyService,
                           ApplicationEventPublisher eventPublisher,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.redissonClient = redissonClient;
        this.queryKeyService = queryKeyService;
        this.eventPublisher = eventPublisher;
        this.userMapper = userMapper;
    }

    /**
     * Registers a new user in the system.
     * @param userRegisterRequest the user registration request containing user details
     * @return UserRegisterResponse containing the created user information
     */
    @Transactional
    @Override
    public UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest) {
        final String normalizedUsername = userRegisterRequest.username().trim().toLowerCase();
        final String lockKey = queryKeyService.lockKey("auth-register",1, normalizedUsername);
        RLock lock = redissonClient.getLock(lockKey);

        log.info("Starting user registration for username: {}", normalizedUsername);

        try {
            if(!lock.tryLock(2,10, TimeUnit.SECONDS)) {
                log.warn("Registration request throttled for username: {}", normalizedUsername);
                throw new LockAcquisitionException("Request Throttled", new SQLException("Request Throttled"));
            }

            if (userRepository.existsByUsername(normalizedUsername)) {
                log.warn("Registration failed - user already exists: {}", normalizedUsername);
                throw new UserAlreadyExistsException("User already exists");
            }

            UserEntity userEntity = new UserEntity();
            userEntity.setUserID(UUID.randomUUID().toString());
            userEntity.setUsername(userRegisterRequest.username());
            userEntity.setPassword(passwordEncoder.encode(userRegisterRequest.password()));
            userEntity.setName(userRegisterRequest.name());
            userEntity.setLastname(userRegisterRequest.lastname());
            userEntity.setDeleted(Boolean.FALSE);
            userEntity.setLastPasswordUpdate(new Date());

            RoleEntity roleEntity = roleRepository
                                    .findByName("ROLE_USER")
                                    .orElseThrow(() -> new RoleNotFoundException("Role not found"));
            userEntity.setRoles(Set.of(roleEntity));

            var savedUserEntity = userRepository.save(userEntity);
            log.info("User registration successful - userId: {}, username: {}", 
                    savedUserEntity.getUserID(), savedUserEntity.getUsername());

            return new UserRegisterResponse(
                                savedUserEntity.getUserID(),
                                savedUserEntity.getUsername(),
                                savedUserEntity.getName(),
                                savedUserEntity.getLastname());


        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Registration interrupted for username: {}", normalizedUsername);
            throw new UserAccountNotCreatedException("Interrupted");
        } catch (DataIntegrityViolationException e) {
            log.warn("Registration failed - data integrity violation for username: {} - reason: {}", normalizedUsername, e.getMessage());
            throw new UserAlreadyExistsException("User already exists");
        }  catch (RoleNotFoundException ex) {
            log.warn("Registration failed - role not found for username: {}", normalizedUsername);
            throw new UserAccountNotCreatedException("Role not found");
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    /**
     * Edits role authorities by adding or removing permissions.
     * @param request the request containing role name, operation, and authorities
     * @return Boolean indicating success of the operation
     */
    @Transactional
    @Override
    public Boolean editRoleAuthorities(RoleAuthorityEditRequestModel request) {
        String roleName = request.roleName();
        String lockKey = queryKeyService.lockKey("role-edit",1, roleName);
        RLock lock = redissonClient.getLock(lockKey);
        
        log.info("Starting role authority edit - role: {}, operation: {}, authorities: {}", 
                roleName, request.operation(), request.authorities());
        
        try {
            if(!lock.tryLock(2,10,TimeUnit.SECONDS)) {
                log.warn("Role authority edit request throttled for role: {}", roleName);
                throw new LockAcquisitionException("Request Throttled", new SQLException("Request Throttled"));
            }
            List<String> authorities = request.authorities();
            boolean add = "add".equalsIgnoreCase(request.operation());

            RoleEntity role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RoleNotFoundException("Role not found"));


            Set<AuthorityEntity> toApply = authorities.stream()
                    .map(name -> authorityRepository.findByName(name)
                            .orElseThrow(() -> new AuthorityNotFoundException("Authority not found")))
                    .filter(Objects::nonNull)
                    .collect(toSet());

            if (add) {
                role.getAuthorities().addAll(toApply);
                log.info("Successfully added {} authorities to role: {}", toApply.size(), roleName);
            } else {
                role.getAuthorities().removeAll(toApply);
                log.info("Successfully removed {} authorities from role: {}", toApply.size(), roleName);
            }

            roleRepository.save(role);
            return Boolean.TRUE;
        } catch (RoleNotFoundException e) {
            log.error("Role authority edit failed - role not found: {}", roleName);
            throw new AuthoritiesNotEditedException("Authorities not updated");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Role authority edit interrupted for role: {}", roleName);
            throw new AuthoritiesNotEditedException("Interrupted");
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }


    /**
     * Deletes a user account and all associated data.
     * @param userId the unique identifier of the user to delete
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "houseAds", key = "#userId")
    })
    @Transactional
    public void deleteAccount(String userId) {
        log.info("Starting account deletion for userId: {}", userId);
        String lockKey = queryKeyService.lockKey("auth-delete",1, userId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if(!lock.tryLock(2,10,TimeUnit.SECONDS)) {
                log.warn("User Delete request throttled for userId: {}", userId);
                throw new LockAcquisitionException("Request Throttled", new SQLException("Request Throttled"));
            }

            var userEntity = userRepository
                    .findByUserID(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            var storageKeys = userEntity.getHouseAds().stream()
                    .flatMap(houseAd -> houseAd.getImages().stream())
                    .map(HouseAdImageEntity::getStorageKey)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            userRepository.delete(userEntity);
            log.info("User account deleted successfully - userId: {}, username: {}",
                    userId, userEntity.getUsername());

            if (!storageKeys.isEmpty()) {
                    eventPublisher.publishEvent(new ImageDeleteBatchEvent(storageKeys));
                    log.info("Queued {} S3 images for deletion - userId: {}", storageKeys.size(), userId);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("User Account delete interrupted for role: {}", userId);
            throw new UserAccountNotDeletedException("Interrupted");
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    /**
     * Retrieves a user by their username.
     * @param username the username to search for
     * @return UserDTO containing user information
     */
    @Override
    @Cacheable(value = "users", key = "#username")
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        log.debug("Retrieving user by username: {}", username);
        
        return userRepository
                .findByUsername(username)
                .map(userMapper)
                .orElseThrow(() -> {
                    log.warn("User not found by username: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
    }

    /**
     * Retrieves a user by their unique identifier.
     * @param userId the unique user identifier
     * @return UserDTO containing user information
     */
    @Override
    @Cacheable(value = "users", key = "#userId")
    @Transactional(readOnly = true)
    public UserDTO getUserById(String userId) {
        log.debug("Retrieving user by userId: {}", userId);
        
        var userDTO = userRepository
                    .findByUserID(userId)
                    .map(userMapper)
                    .orElseThrow(() -> {
                        log.warn("User not found by userId: {}", userId);
                        return new UserIDNotFoundException("User not found");
                    });
        
        log.debug("User retrieved successfully - userId: {}, username: {}", 
                userDTO.getUserID(), userDTO.getUsername());
        return userDTO;
    }

    /**
     * Adds or updates the billing address for a user.
     * @param userId the unique identifier of the user
     * @param model the address creation request model containing address details
     * @return Boolean indicating success of the operation
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    @Override
    public Boolean addOrUpdateBillingAddress(String userId, AddressCreationRequestModel model) {
        log.info("Starting billing address update for userId: {}", userId);

        final String lockKey = queryKeyService.lockKey("address-update", 1, userId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(2, 10, TimeUnit.SECONDS)) {
                log.warn("Address update request throttled for userId: {}", userId);
                throw new LockAcquisitionException("Request Throttled", new SQLException("Request Throttled"));
            }

            UserEntity user = userRepository.findByUserID(userId)
                    .orElseThrow(() -> new UserIDNotFoundException("User not found"));

                AddressEntity addr = user.getBillingAddress();
                if (addr == null) {
                    addr = new AddressEntity();
                    addr.setAddressID(UUID.randomUUID().toString());
                    user.setBillingAddress(addr);
                    addr.setBilledUser(user);
                    log.debug("Created new billing address for userId: {}", userId);
                } else {
                    log.debug("Updating existing billing address for userId: {}", userId);
                }

                if (model.billingStreet() != null)
                    addr.setStreet(model.billingStreet());
                if (model.billingCity() != null)
                    addr.setCity(model.billingCity());
                if (model.billingState() != null)
                    addr.setState(model.billingState());
                if (model.billingZip() != null)
                    addr.setZip(model.billingZip());

                userRepository.save(user);
                log.info("Billing address updated successfully - userId: {}, addressId: {}",
                        userId, addr.getAddressID());

            return Boolean.TRUE;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Address update interrupted for role: {}", userId);
                throw new UserAccountNotDeletedException("Interrupted");
            } catch (Exception e) {
                log.error("Billing address update failed for userId: {} - error: {}", userId, e.getMessage(), e);
                throw new AddressAddingException("Error adding address");
            } finally {
                if (lock.isHeldByCurrentThread())
                    lock.unlock();
            }
        }


    /**
     * Loads user details for authentication.
     * @param username the username to load
     * @return UserDetails containing user authentication information
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) {
        log.debug("Loading user details for authentication - username: {}", username);
        
        if (!userRepository.existsByUsername(username)) {
            log.warn("Authentication failed - user not found: {}", username);
            throw new UsernameNotFoundException("User not found");
        }

        return userRepository
                .findByUsername(username)
                .map(User::new)
                .orElseThrow(() -> {
                    log.error("Authentication failed - user exists but couldn't be loaded: {}", username);
                    return new UsernameNotFoundException("User couldn't be fetched");
                });
    }

}
