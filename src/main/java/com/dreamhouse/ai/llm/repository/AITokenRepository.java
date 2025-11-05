package com.dreamhouse.ai.llm.repository;


import com.dreamhouse.ai.llm.entity.AITokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AITokenRepository extends JpaRepository<AITokenEntity, Long> {
    Optional<AITokenEntity> findByToken(@Param("token") String token);
    Optional<AITokenEntity> findByUserId(@Param("userId") String userId);
}
