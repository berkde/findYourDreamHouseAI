package com.dreamhouse.ai.authentication.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import java.util.Objects;

public record UserRegisterRequest(@JsonProperty("username") String username,
                                  @JsonProperty("password") String password,
                                  @JsonProperty("name") String name,
                                  @JsonProperty("lastname") String lastname) {
    public UserRegisterRequest {
        Assert.hasText(username, "Username must not be empty");
        Assert.hasText(password, "Password must not be empty");
        Assert.hasText(name, "Name must not be empty");
        Assert.hasText(lastname, "Lastname must not be empty");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserRegisterRequest(String username1, String password1, String name1, String lastname1))) return false;
        return Objects.equals(name, name1) && Objects.equals(username, username1) && Objects.equals(password, password1) && Objects.equals(lastname, lastname1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, name, lastname);
    }
}
