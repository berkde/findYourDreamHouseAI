package com.dreamhouse.ai.listener.event;

import com.dreamhouse.ai.llm.exception.NoUserOrPlanCodeException;

import java.util.Objects;

public record AIPlanUpdateEvent(String userId, String planCode) {
    public AIPlanUpdateEvent {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(planCode, "planCode cannot be null");

        if (userId.trim().isEmpty() || planCode.trim().isEmpty()) {
            throw new NoUserOrPlanCodeException("userId and planCode cannot be empty");
        }
    }
}
