package com.dreamhouse.ai.authentication.exception;

public class UserIDNotFoundException extends RuntimeException {
    public UserIDNotFoundException(String message) {
        super(message);
    }
}
