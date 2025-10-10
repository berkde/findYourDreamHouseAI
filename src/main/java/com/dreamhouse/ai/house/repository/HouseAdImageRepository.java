package com.dreamhouse.ai.house.repository;

import com.dreamhouse.ai.house.model.entity.HouseAdImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HouseAdImageRepository extends JpaRepository<HouseAdImageEntity, Long> {
}
