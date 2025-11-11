package com.dreamhouse.ai.listener.event;

import com.dreamhouse.ai.llm.exception.NoUserOrPlanCodeException;

import java.util.Objects;

/**
 * Domain event indicating that a user's AI subscription plan has changed.
 * <p>
 * This record is typically published inside a transactional context; listeners
 * can react after the surrounding transaction commits to update related
 * resources such as token plans and quotas.
 * </p>
 *
 * <p>Validation:</p>
 * <ul>
 *   <li>{@code userId} and {@code planCode} must be non-null and non-blank.</li>
 *   <li>If validation fails, a NoUserOrPlanCodeException is thrown.</li>
 * </ul>
 *
 * @param userId   the unique identifier of the user whose plan changed
 * @param planCode the new plan code assigned to the user
 */
public record AIPlanUpdateEvent(String userId, String planCode) {
    /**
     * Compact constructor performing null/blank validation and throwing
     * a NoUserOrPlanCodeException when either field is invalid.
     */
    public AIPlanUpdateEvent {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(planCode, "planCode cannot be null");

        if (userId.trim().isEmpty() || planCode.trim().isEmpty()) {
            throw new NoUserOrPlanCodeException("userId and planCode cannot be empty");
        }
    }
}
