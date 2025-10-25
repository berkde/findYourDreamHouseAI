package com.dreamhouse.ai.house.exception;

public class SecretParsingException extends RuntimeException {
    public SecretParsingException(String message, Exception e) {
        super(message);
    }
}
