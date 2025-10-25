package com.dreamhouse.ai.house.exception;

import com.fasterxml.jackson.core.JsonParseException;

public class SecretFormatException extends RuntimeException {
    public SecretFormatException(String message, JsonParseException e) {
        super(message);
    }
}
