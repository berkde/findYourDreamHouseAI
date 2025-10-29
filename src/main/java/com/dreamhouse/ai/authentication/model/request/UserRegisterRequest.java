package com.dreamhouse.ai.authentication.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.util.Assert;

import java.util.Objects;

public record UserRegisterRequest(
        @JsonProperty("username") 
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Email
        String username,
        
        @JsonProperty("password") 
        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must contain at least 8 characters with uppercase, lowercase, number and special character")
        String password,
        
        @JsonProperty("name") 
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,
        
        @JsonProperty("lastname") 
        @NotBlank(message = "Lastname is required")
        @Size(min = 2, max = 100, message = "Lastname must be between 2 and 100 characters")
        String lastname
) {
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
