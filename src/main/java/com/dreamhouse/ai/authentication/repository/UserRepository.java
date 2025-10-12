package com.dreamhouse.ai.authentication.repository;

import com.dreamhouse.ai.authentication.model.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    @EntityGraph(attributePaths = {"roles", "roles.authorities", "billingAddress"})
    Optional<UserEntity> findByUsername(String username);
    @EntityGraph(attributePaths = {"roles", "roles.authorities", "billingAddress"})
    Optional<UserEntity> findByUserID(String userID);
    Boolean existsByUsername(String username);
    List<UserEntity> findAll();
}
