package com.dreamhouse.ai.authentication.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.util.Assert;

import java.util.Objects;

public record UserRegisterRequest(
        @JsonProperty("username") 
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        
        @JsonProperty("password") 
        @NotBlank(message = "Password is required")
        String password,
        
        @JsonProperty("name") 
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,
        
        @JsonProperty("lastname") 
        @NotBlank(message = "Lastname is required")
        @Size(min = 2, max = 100, message = "Lastname must be between 2 and 100 characters")
        String lastname,
        
        @JsonProperty("email") 
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email
) {
    public UserRegisterRequest {
        Assert.hasText(username, "Username must not be empty");
        Assert.hasText(password, "Password must not be empty");
        Assert.hasText(name, "Name must not be empty");
        Assert.hasText(lastname, "Lastname must not be empty");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserRegisterRequest(String username1, String password1, String name1, String lastname1, String email1))) return false;
        return Objects.equals(name, name1) && Objects.equals(username, username1) && Objects.equals(password, password1) && Objects.equals(lastname, lastname1) && Objects.equals(email, email1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, name, lastname, email);
    }
}
