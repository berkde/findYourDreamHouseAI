package com.dreamhouse.ai.authentication.exception;

public class AuthenticatedUserNotFound extends RuntimeException {
    public AuthenticatedUserNotFound(String message) {
        super(message);
    }
}
