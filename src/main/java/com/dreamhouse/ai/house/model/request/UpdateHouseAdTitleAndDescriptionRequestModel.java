package com.dreamhouse.ai.house.model.request;


import java.util.Objects;

public record UpdateHouseAdTitleAndDescriptionRequestModel(
        String houseAdId,
        String title,
        String description) {

    public UpdateHouseAdTitleAndDescriptionRequestModel {
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UpdateHouseAdTitleAndDescriptionRequestModel that)) return false;
        return Objects.equals(houseAdId, that.houseAdId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(houseAdId);
    }
}
