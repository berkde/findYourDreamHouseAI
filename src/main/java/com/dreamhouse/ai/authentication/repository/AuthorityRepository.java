package com.dreamhouse.ai.authentication.repository;

import com.dreamhouse.ai.authentication.model.entity.AuthorityEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends CrudRepository<AuthorityEntity, Long> {
    Optional<AuthorityEntity> findByName(String name);
}
