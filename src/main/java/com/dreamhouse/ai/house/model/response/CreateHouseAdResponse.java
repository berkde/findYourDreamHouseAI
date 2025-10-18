package com.dreamhouse.ai.house.model.response;

import java.util.Objects;

public record CreateHouseAdResponseModel(String houseAdId, String title) {
    public CreateHouseAdResponseModel {}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CreateHouseAdResponseModel(String adId, String title1))) return false;
        return Objects.equals(title, title1) && Objects.equals(houseAdId, adId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(houseAdId, title);
    }
}
