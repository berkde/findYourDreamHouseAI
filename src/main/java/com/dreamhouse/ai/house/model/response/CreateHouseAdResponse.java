package com.dreamhouse.ai.house.model.response;

import java.util.Objects;

public record CreateHouseAdResponse(String houseAdId, String title) {
    public CreateHouseAdResponse {
        Objects.requireNonNull(houseAdId, "House Ad ID is required");
        Objects.requireNonNull(title, "Title is required");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CreateHouseAdResponse(String adId, String title1))) return false;
        return Objects.equals(title, title1) && Objects.equals(houseAdId, adId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseAdId, title);
    }
}
