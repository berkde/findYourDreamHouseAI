package com.dreamhouse.ai.llm.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record AITokenDTO(
        @JsonProperty("token")
        @NotBlank(message = "token cannot be blank")
        @Size(min = 32, max = 32, message = "token must be exactly 32 characters")
        @Pattern(regexp = "^[a-fA-F0-9]{32}$", message = "token must be 32 hexadecimal characters")
        String token
) {

    public AITokenDTO {
        token = token.trim();
    }
}
