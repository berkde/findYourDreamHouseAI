package com.dreamhouse.ai.mapper;

import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.model.entity.HouseAdImageEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class HouseAdImageMapper implements Function<HouseAdImageEntity, HouseAdImageDTO> {
    @Override
    public HouseAdImageDTO apply(@NotNull HouseAdImageEntity houseAdImageEntity) {
        var image = new HouseAdImageDTO();
        image.setHouseAdImageUid(houseAdImageEntity.getHouseAdImageUid());
        image.setImageType(houseAdImageEntity.getImageType());
        image.setImageName(houseAdImageEntity.getImageName());
        image.setImageURL(houseAdImageEntity.getImageURL());
        image.setStorageKey(houseAdImageEntity.getStorageKey());
        image.setImageDescription(houseAdImageEntity.getImageDescription());
        image.setImageThumbnail(houseAdImageEntity.getImageThumbnail());
        return image;
    }
}
