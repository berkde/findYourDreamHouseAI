package com.dreamhouse.ai.llm.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Component
public class AIUtil {
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

    public long millisUntilEndOfMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.toLocalDate().withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .atTime(23, 59, 59, 999_000_000);
        return Duration.between(now, end).toMillis();
    }

}
