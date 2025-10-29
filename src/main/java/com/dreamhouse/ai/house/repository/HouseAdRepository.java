package com.dreamhouse.ai.house.repository;

import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseAdRepository extends JpaRepository<HouseAdEntity, Long>, JpaSpecificationExecutor<HouseAdEntity> {
    @EntityGraph(attributePaths = {"images", "messages"})
    Optional<HouseAdEntity> findByHouseAdUid(String houseAdId);
    @Query("""
        select h from HouseAdEntity h
        where lower(h.title) like lower(concat('%', :q, '%'))
           or lower(h.description) like lower(concat('%', :q, '%'))
    """)
    Page<HouseAdEntity> searchTitleOrDescription(@Param("q") String q, Pageable pageable);
    List<HouseAdEntity> findAllByDescriptionContainingIgnoreCase(String description);

    @EntityGraph(attributePaths = {"images"})
    @Query("select h from HouseAdEntity h")
    @NonNull List<HouseAdEntity> findAll();

    @NotNull
    @Override
    @EntityGraph(attributePaths = "images")
    Page<HouseAdEntity> findAll(@Nullable Specification<HouseAdEntity> spec,
                                @NotNull Pageable pageable);
}
