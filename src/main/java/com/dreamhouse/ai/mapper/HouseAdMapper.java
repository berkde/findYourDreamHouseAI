package com.dreamhouse.ai.mapper;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.dreamhouse.ai.house.dto.HouseAdImageDTO;
import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class HouseAdMapper implements Function<HouseAdEntity, HouseAdDTO> {
    private final HouseAdImageMapper houseImageMapper;

    public HouseAdMapper(HouseAdImageMapper houseImageMapper) {
        this.houseImageMapper = houseImageMapper;
    }

    @Override
    public HouseAdDTO apply(@NotNull HouseAdEntity houseAd) {
        var houseAdDTO = new HouseAdDTO();
        houseAdDTO.setHouseAdUid(houseAd.getHouseAdUid());
        houseAdDTO.setTitle(houseAd.getTitle());
        houseAdDTO.setDescription(houseAd.getDescription());
        houseAdDTO.setCity(houseAd.getCity());

        if(houseAd.getImages() != null) {
            List<HouseAdImageDTO> images = houseAd.getImages()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(houseImageMapper)
                    .toList();
            houseAdDTO.setImages(images);
        } else {
            houseAdDTO.setImages(Collections.emptyList());
        }

        if(houseAd.getLikes() != null) {
            var likedUsers = houseAd.getLikes()
                    .stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            houseAdDTO.setLikedUsers(likedUsers);
        } else {
            houseAdDTO.setLikedUsers(Collections.emptyList());
        }

        return houseAdDTO;
    }
}
