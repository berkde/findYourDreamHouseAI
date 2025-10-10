package com.dreamhouse.ai.authentication.service.impl;

import com.dreamhouse.ai.authentication.dto.UserDTO;
import com.dreamhouse.ai.authentication.exception.*;
import com.dreamhouse.ai.authentication.model.entity.AddressEntity;
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
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           AuthorityRepository authorityRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public UserRegisterResponse registerUser(UserRegisterRequest userRegisterRequest) {
        if (userRepository.existsByUsername(userRegisterRequest.username())) {
            log.warn("registerUser - User already exists: {}", userRegisterRequest.username());
            throw new UserAlreadyExistsException("User already exists");
        }

        try {
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
                    savedUserEntity.getLastname());
        } catch (Exception e) {
            log.error("registerUser - Error creating user account: {}", userRegisterRequest.username(), e);
            throw new UserAccountNotCreatedException("Error creating user account: " + e.getMessage() + " " + userRegisterRequest.username() + " " + userRegisterRequest.password());
        }
    }

    @Transactional
    @Override
    public Boolean editRoleAuthorities(RoleAuthorityEditRequestModel roleAuthorityEditRequest) {
        String roleName = roleAuthorityEditRequest.roleName();
        List<String> authorities = roleAuthorityEditRequest.authorities();
        String operation = roleAuthorityEditRequest.operation();

        try {
            var roleEntity = roleRepository
                    .findByName(roleName)
                    .orElseThrow(() -> new RoleNotFoundException("Role not found"));


            if (operation.equals("add")) {
                authorities.forEach(authorityName -> {
                    var authorityEntity = authorityRepository
                            .findByName(authorityName)
                            .orElseThrow(() -> new AuthorityNotFoundException("Authority not found"));
                    roleEntity.getAuthorities().add(authorityEntity);
                    roleRepository.save(roleEntity);
                    log.info("editRoleAuthorities - Authority {} added to role {}", authorityName, roleName);
                });
            } else {
                authorities.forEach(authorityName -> {
                    var authorityEntity = authorityRepository
                            .findByName(authorityName)
                            .orElseThrow(() -> new AuthorityNotFoundException("Authority not found"));
                    roleEntity.getAuthorities().remove(authorityEntity);
                    roleRepository.save(roleEntity);
                    log.info("editRoleAuthorities - Authority {} removed from role {}", authorityName, roleName);
                });
            }

            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("editRoleAuthorities - Error editing authorities: {}", roleName, e);
            throw new AuthoritiesNotEditedException("Error editing authorities: " + e.getMessage() + " " + roleName + " " + authorities + " " + operation);
        }
    }


    @Override
    @CacheEvict(value = "users", allEntries = true)
    public Boolean deleteAccount(String userId) {
        try {
            UserEntity userEntity = userRepository.findByUserID(userId).orElseThrow(() -> new UserIDNotFoundException("User not found"));
            userRepository.delete(userEntity);
            log.info("deleteAccount - User deleted: {}", userId);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("deleteAccount - Error deleting user: {}", userId, e);
            return Boolean.FALSE;
        }
    }

    @Override
    @Cacheable(value = "users", key = "#username")
    public UserDTO getUserByUsername(String username) {
        try {
            var userEntity = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            log.info("getUserByUsername - User found: {}", userEntity);
            return modelMapper.map(userEntity, UserDTO.class);
        } catch (Exception e) {
            log.error("getUserByUsername - Error finding user: {}", username, e);
            throw new UsernameNotFoundException("User not found: " + username + " " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "users", key = "#userId")
    public UserDTO getUserById(String userId) {
        try {
            var userEntity = userRepository.findByUserID(userId).orElseThrow(() -> new UserIDNotFoundException("User not found"));
            log.info("getUserById - User found: {}", userEntity);
            return modelMapper.map(userEntity, UserDTO.class);
        } catch (Exception e) {
            log.error("getUserById - Error finding user: {}", userId, e);
            throw new UserIDNotFoundException("User not found: " + userId + " " + e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    @Override
    public Boolean addOrUpdateBillingAddress(String userId, AddressCreationRequestModel model){
        UserEntity user = userRepository.findByUserID(userId)
                .orElseThrow(() -> new UserIDNotFoundException("User not found: " + userId));

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

            log.info("addAddress - Address added: {}", addr);
            userRepository.save(user);

            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("addAddress - Error adding address: {}", userId, e);
            throw new AddressAddingException("Error adding address: " + e.getMessage() + " " + userId + " " + model.billingStreet() + " " + model.billingCity() + " " + model.billingState() + " " + model.billingZip());
        }
    }


    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!userRepository.existsByUsername(username)) {
            log.warn("loadUserByUsername - User not found: {}", username);
            throw new UsernameNotFoundException("User not found");
        }

        var userEntity =  userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        log.info("loadUserByUsername - User found: {}", userEntity.getUsername());
        return new User(userEntity);
    }

}
