package com.dreamhouse.ai.cloud.exception;

import com.fasterxml.jackson.core.JsonParseException;

public class SecretFormatException extends RuntimeException {
    public SecretFormatException(String message, JsonParseException e) {
        super(message);
    }
}
