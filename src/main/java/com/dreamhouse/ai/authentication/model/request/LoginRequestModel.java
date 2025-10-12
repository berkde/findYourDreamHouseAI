package com.dreamhouse.ai.authentication.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.util.Assert;

import java.util.Objects;

public record LoginRequestModel(
        @JsonProperty("username") 
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        
        @JsonProperty("password") 
        @NotBlank(message = "Password is required")
        @Size(min = 1, max = 255, message = "Password must not exceed 255 characters")
        String password
) {
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
