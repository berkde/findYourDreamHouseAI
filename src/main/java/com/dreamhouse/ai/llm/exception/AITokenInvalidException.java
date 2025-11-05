package com.dreamhouse.ai.llm.exception;

public class AITokenInvalidException extends RuntimeException {
    public AITokenInvalidException(String message) {
        super(message);
    }
}
