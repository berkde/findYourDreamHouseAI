package com.dreamhouse.ai.house.repository;

import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseAdRepository extends JpaRepository<HouseAdEntity, Long> {
    @EntityGraph(attributePaths = {"images", "messages"})
    Optional<HouseAdEntity> findByHouseAdUid(String houseAdId);
    Optional<HouseAdEntity> findByTitle(String title);
    List<HouseAdEntity> findAllByTitleContainingIgnoreCase(String title);
    List<HouseAdEntity> findAllByDescriptionContainingIgnoreCase(String description);

    @EntityGraph(attributePaths = {"images"})   // add more if you need
    @Query("select h from HouseAdEntity h")
    @NonNull List<HouseAdEntity> findAll();
}
