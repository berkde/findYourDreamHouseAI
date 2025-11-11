package com.dreamhouse.ai.llm.repository;


import com.dreamhouse.ai.llm.entity.AITokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for persisting and querying {@link AITokenEntity} instances.
 * Provides convenience finder methods to retrieve tokens either by their opaque token value
 * or by the associated user identifier.
 */
@Repository
public interface AITokenRepository extends JpaRepository<AITokenEntity, Long> {

    /**
     * Finds a token entity by its opaque token string.
     *
     * @param token the token value to look up
     * @return an {@link Optional} with the token entity if present; otherwise empty
     */
    Optional<AITokenEntity> findByToken(@Param("token") String token);

    /**
     * Finds a token entity by the owning user's identifier.
     *
     * @param userId the unique identifier of the user
     * @return an {@link Optional} with the token entity if present; otherwise empty
     */
    Optional<AITokenEntity> findByUserId(@Param("userId") String userId);
}
