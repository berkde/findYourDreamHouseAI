package com.dreamhouse.ai.llm.exception;

public class NoUserOrPlanCodeException extends RuntimeException {
    public NoUserOrPlanCodeException(String message) {
        super(message);
    }
}
