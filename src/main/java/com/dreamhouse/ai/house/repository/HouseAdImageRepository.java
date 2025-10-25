package com.dreamhouse.ai.house.repository;

import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import com.dreamhouse.ai.house.model.entity.HouseAdImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HouseAdImageRepository extends JpaRepository<HouseAdImageEntity, Long> {
    List<HouseAdImageEntity> findAllByHouseAd(HouseAdEntity houseAd);
}
