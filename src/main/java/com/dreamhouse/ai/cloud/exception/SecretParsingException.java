package com.dreamhouse.ai.cloud.exception;

public class SecretParsingException extends RuntimeException {
    public SecretParsingException(String message, Exception e) {
        super(message);
    }
}
