package com.dreamhouse.ai.house.model.response;

import com.dreamhouse.ai.authentication.dto.UserDTO;

import java.util.List;
import java.util.Objects;

public record LikeHouseAdResponse(String houseAdUid, List<UserDTO> users) {
    public LikeHouseAdResponse {
        Objects.requireNonNull(houseAdUid, "House ad uid cannot be null");
        Objects.requireNonNull(users, "Users cannot be null");

        if(houseAdUid.trim().isBlank()) {
            throw new IllegalArgumentException("House ad uid cannot be empty");
        }

        if(users.isEmpty()) {
            throw new IllegalArgumentException("Users cannot be empty");
        }

        users = List.copyOf(users);
    }
}
