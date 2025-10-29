package com.dreamhouse.ai.house.model.request;

import com.dreamhouse.ai.house.dto.HouseAdImageDTO;

import java.util.List;
import java.util.Objects;

public record AddHouseAdImagesRequestModel(String houseAdId, List<HouseAdImageDTO> images) {
    public AddHouseAdImagesRequestModel {
        Objects.requireNonNull(houseAdId, "House ad id is required");
        Objects.requireNonNull(images, "Images is required");

        images = List.copyOf(images);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AddHouseAdImagesRequestModel(String adId, List<HouseAdImageDTO> images1))) return false;
        return Objects.equals(houseAdId, adId) && Objects.equals(images, images1);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(houseAdId);
    }
}
