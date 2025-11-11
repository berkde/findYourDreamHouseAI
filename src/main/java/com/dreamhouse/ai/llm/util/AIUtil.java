package com.dreamhouse.ai.llm.util;

import com.dreamhouse.ai.authentication.exception.AuthenticatedUserNotFound;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility component with helper methods used across AI-related flows.
 */
@Component
public class AIUtil {

    /**
     * Resolves a stable session identifier for rate-limiting, caching, or telemetry purposes.
     * Resolution order:
     *   If {@code headerSessionId} is non-blank, returns its trimmed value.
     *   Else if {@code principal} is present and has a name, returns {@code "user:" + principalName}.
     *   Else uses the current HTTP session id (creating a session if necessary) and returns {@code "sess:" + id}.
     *
     *
     *
     * @param headerSessionId optional session id supplied via request headers
     * @param principal       the authenticated principal (may be {@code null})
     * @param request         the current HTTP servlet request
     * @return a non-blank, prefixed session identifier such as {@code user:alice} or {@code sess:abc123}
     */
    public String resolveSessionId(String headerSessionId, Principal principal, HttpServletRequest request) {
        if (headerSessionId != null && !headerSessionId.isBlank()) {
            return headerSessionId.trim();
        }
        if (principal != null && principal.getName() != null) {
            return "user:" + principal.getName();
        }
        String httpSessionId = Objects.requireNonNullElseGet(
                request.getSession(true).getId(),
                () -> UUID.randomUUID().toString()
        );
        return "sess:" + httpSessionId;
    }

    /**
     * Computes the number of milliseconds from now until the end of the current calendar month
     * (23:59:59.999 local time on the last day).
     *
     * @return remaining milliseconds until the end of the month
     */
    public long millisUntilEndOfMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.toLocalDate().withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .atTime(23, 59, 59, 999_000_000);
        return Duration.between(now, end).toMillis();
    }

    /**
     * Returns the username of the currently authenticated user from Spring Security's context.
     * <p>
     * This method reads the current {@code Authentication} from SecurityContextHolder and returns its {@code name}.
     * If the context is not populated (for example, no request-bound authentication, anonymous/invalid authentication,
     * or the security filter chain has not run), this method throws an AuthenticatedUserNotFound at runtime.
     * </p>
     *
     * @return the non-blank username associated with the current authentication
     */
    public String getAuthenticatedUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.isBlank()) {
            throw new AuthenticatedUserNotFound("Authentication failed");
        }
        return username;
    }

}
