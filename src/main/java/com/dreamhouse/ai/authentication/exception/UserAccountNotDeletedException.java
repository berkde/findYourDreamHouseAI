package com.dreamhouse.ai.authentication.exception;

public class UserAccountNotDeletedException extends RuntimeException {
    public UserAccountNotDeletedException(String message) {
        super(message);
    }
}
