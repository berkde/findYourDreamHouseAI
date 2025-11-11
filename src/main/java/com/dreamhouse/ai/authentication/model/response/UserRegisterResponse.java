package com.dreamhouse.ai.authentication.model.response;

import java.util.Objects;

public record UserRegisterResponse(String userID, String username, String name, String lastname, String type, String aiAuthToken) {
    public UserRegisterResponse {
        Objects.requireNonNull(userID, "UserID is required");
        Objects.requireNonNull(username, "Username is required");
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(lastname, "Last name is required");
        Objects.requireNonNull(type, "Type is required");
        Objects.requireNonNull(aiAuthToken, "AiAuthToken is required");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserRegisterResponse(
                String id, String username1, String name1, String lastname1, String type1, String authToken
        ))) return false;
        return Objects.equals(name, name1) && Objects.equals(type, type1) && Objects.equals(userID, id) && Objects.equals(username, username1) && Objects.equals(lastname, lastname1) && Objects.equals(aiAuthToken, authToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, username, name, lastname, type, aiAuthToken);
    }
}
