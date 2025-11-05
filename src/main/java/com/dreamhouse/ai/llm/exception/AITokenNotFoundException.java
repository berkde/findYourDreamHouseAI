package com.dreamhouse.ai.llm.exception;

public class AITokenNotFoundException extends RuntimeException {
    public AITokenNotFoundException(String message) {
        super(message);
    }
}
