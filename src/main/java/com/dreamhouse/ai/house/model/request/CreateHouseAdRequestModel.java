package com.dreamhouse.ai.house.model.request;

import com.dreamhouse.ai.house.dto.HouseAdImageDTO;

import java.util.List;
import java.util.Objects;

public record CreateHouseAdRequestModel(
        String title,
        String description,
        String type,
        String state,
        String city,
        String neighborhood,
        int beds,
        int baths,
        int sqft,
        Boolean parking,
        Boolean petsAllowed,
        Boolean waterfront,
        int yearBuilt,
        List<HouseAdImageDTO> images) {

    public CreateHouseAdRequestModel {
        Objects.requireNonNull(title, "Title is required");
        Objects.requireNonNull(description, "Description is required");
        Objects.requireNonNull(type, "Type is required");
        Objects.requireNonNull(state, "State is required");
        Objects.requireNonNull(city, "City is required");
        Objects.requireNonNull(neighborhood, "Neighborhood is required");

        if(images != null) {
            images = List.copyOf(images);
        }

        if(beds < 0) {
            throw new IllegalArgumentException("Beds cannot be negative");
        }
        if(baths < 0) {
            throw new IllegalArgumentException("Baths cannot be negative");
        }
        if(sqft < 0) {
            throw new IllegalArgumentException("Square footage cannot be negative");
        }
        if (yearBuilt < 0) {
            throw new IllegalArgumentException("Year built cannot be negative");
        }

        parking = (parking != null) ? parking : Boolean.FALSE;

        petsAllowed = (petsAllowed != null) ? petsAllowed : Boolean.FALSE;

        waterfront = (waterfront != null) ? waterfront : Boolean.FALSE;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CreateHouseAdRequestModel that)) return false;
        return beds == that.beds && sqft == that.sqft && baths == that.baths && Objects.equals(type, that.type) && Objects.equals(city, that.city) && Objects.equals(title, that.title) && Objects.equals(state, that.state) && Objects.equals(description, that.description) && Objects.equals(neighborhood, that.neighborhood);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, type, state, city, neighborhood, beds, baths, sqft);
    }
}
