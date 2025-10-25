package com.dreamhouse.ai.house.model.request;

import com.dreamhouse.ai.house.dto.HouseAdImageDTO;

import java.util.List;
import java.util.Objects;

public record CreateHouseAdRequestModel(
        String title,
        String description,
        String city,
        List<HouseAdImageDTO> images) {

    public CreateHouseAdRequestModel {
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CreateHouseAdRequestModel that)) return false;
        return Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(city, that.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, city);
    }
}
