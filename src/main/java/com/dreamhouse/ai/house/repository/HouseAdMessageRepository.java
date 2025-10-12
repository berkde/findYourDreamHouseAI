package com.dreamhouse.ai.house.repository;

import com.dreamhouse.ai.house.model.entity.HouseAdMessageEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseAdMessageRepository extends CrudRepository<HouseAdMessageEntity, Long> {
    @EntityGraph(attributePaths = {"houseAd"})
    Optional<HouseAdMessageEntity> findByMessageUid(String messageUid);

}
