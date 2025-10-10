package com.dreamhouse.ai.authentication.exception;

public class AccountNotDeletedException extends RuntimeException {
    public AccountNotDeletedException(String message) {
        super(message);
    }
}
