package com.dreamhouse.ai.house.model.request;


import java.util.Objects;

public record UpdateHouseAdTitleAndDescriptionRequestModel(
        String houseAdId,
        String title,
        String description) {

    public UpdateHouseAdTitleAndDescriptionRequestModel {
        Objects.requireNonNull(houseAdId, "House Ad ID is required");
        Objects.requireNonNull(title, "Title is required");
        Objects.requireNonNull(description, "Description is required");
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
