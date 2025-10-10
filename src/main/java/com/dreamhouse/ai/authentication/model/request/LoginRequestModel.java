package com.dreamhouse.ai.authentication.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import java.util.Objects;

public record LoginRequestModel(@JsonProperty("username") String username,
                                @JsonProperty("password") String password) {
    public LoginRequestModel {
        Assert.hasText(username, "Username must not be empty");
        Assert.hasText(password, "Password must not be empty");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LoginRequestModel(String username1, String password1))) return false;
        return Objects.equals(username, username1) && Objects.equals(password, password1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}
