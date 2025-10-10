package com.dreamhouse.ai.authentication.model.response;

import java.util.Objects;

public record UserRegisterResponse(String userID, String username, String name, String lastname) {
    public UserRegisterResponse {}

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserRegisterResponse(String id, String username1, String name1, String lastname1))) return false;
        return Objects.equals(name, name1) && Objects.equals(userID, id) && Objects.equals(username, username1) && Objects.equals(lastname, lastname1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, username, name, lastname);
    }
}
