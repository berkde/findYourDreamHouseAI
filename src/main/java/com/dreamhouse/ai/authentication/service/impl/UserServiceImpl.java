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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    private final ConcurrentHashMap<String, CompletableFuture<Object>> inflight = new ConcurrentHashMap<>();

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

    @Transactional
    @Override
    public UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest) {
        final String normalizedUsername = userRegisterRequest.username().trim().toLowerCase();
        final String lockKey = queryKeyService.lockKey("auth-register",1, normalizedUsername);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if(!lock.tryLock(2,10, TimeUnit.SECONDS)) {
                throw new LockAcquisitionException("Request Throttled", new SQLException("Request Throttled"));
            }

            if (userRepository.existsByUsername(normalizedUsername)) {
                log.warn("registerUser - User already exists");
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

            return new UserRegisterResponse(
                                savedUserEntity.getUserID(),
                                savedUserEntity.getUsername(),
                                savedUserEntity.getName(),
                                savedUserEntity.getLastname(),
                                savedUserEntity.getType(),
                                savedUserEntity.getAiAuthToken());


        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new UserAccountNotCreatedException("Interrupted");
        } catch (DataIntegrityViolationException dup) {
            throw new UserAlreadyExistsException("User already exists");
        } catch (Exception e) {
            log.error("registerUser - error: {}", e.getMessage(), e);
            throw new UserAccountNotCreatedException("Error creating user account");
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    @Transactional
    @Override
    public Boolean editRoleAuthorities(RoleAuthorityEditRequestModel request) {
        String roleName = request.roleName();
        String lockKey = queryKeyService.lockKey("role-edit",1, roleName);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(!lock.tryLock(2,10,TimeUnit.SECONDS)) {
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
            } else {
                role.getAuthorities().removeAll(toApply);
            }

            roleRepository.save(role);

            log.info("editRoleAuthorities - {} authorities on role", add ? "added" : "removed");
            return Boolean.TRUE;
        } catch (RoleNotFoundException e) {
            throw new AuthoritiesNotEditedException("Authorities not updated");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthoritiesNotEditedException("Interrupted");
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }


    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "houseAds", key = "#userId")
    })
    @Transactional
    public void deleteAccount(String userId) {
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
        if (!storageKeys.isEmpty()) {
                eventPublisher.publishEvent(new ImageDeleteBatchEvent(storageKeys));
                log.info("S3 images queued to be deleted");
        }
    }

    @Override
    @Cacheable(value = "users", key = "#username")
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .map(userMapper)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Cacheable(value = "users", key = "#userId")
    @Transactional(readOnly = true)
    public UserDTO getUserById(String userId) {
        var userDTO = userRepository
                    .findByUserID(userId)
                    .map(userMapper)
                    .orElseThrow(() -> new UserIDNotFoundException("User not found"));
        log.info("getUserById - User found: {}", userDTO.getUserID());
        return userDTO;
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    @Override
    public Boolean addOrUpdateBillingAddress(String userId, AddressCreationRequestModel model){
        UserEntity user = userRepository.findByUserID(userId)
                .orElseThrow(() -> new UserIDNotFoundException("User not found"));

        try {
            AddressEntity addr = user.getBillingAddress();
            if (addr == null) {
                addr = new AddressEntity();
                addr.setAddressID(UUID.randomUUID().toString());
                user.setBillingAddress(addr);
                addr.setBilledUser(user);
            }

            if (model.billingStreet() != null)
                addr.setStreet(model.billingStreet());
            if (model.billingCity() != null)
                 addr.setCity(model.billingCity());
            if (model.billingState() != null)
                 addr.setState(model.billingState());
            if (model.billingZip() != null)
                addr.setZip(model.billingZip());

            log.info("addAddress - Address added: {}", addr.getAddressID());
            userRepository.save(user);

            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("addAddress - Error adding address: {}", userId, e);
            throw new AddressAddingException("Error adding address");
        }
    }


    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            log.warn("loadUserByUsername - User not found: {}", username);
            throw new UsernameNotFoundException("User not found");
        }

        return userRepository
                .findByUsername(username)
                .map(User::new)
                .orElseThrow(() -> {
                    log.info("loadUserByUsername - User found");
                    return new UsernameNotFoundException("User couldn't be fetched");
                });
    }

}
