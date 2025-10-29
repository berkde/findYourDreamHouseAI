package com.dreamhouse.ai.llm.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.security.Principal;
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
        // fall back to HTTP session id (creates one if missing)
        String httpSessionId = Objects.requireNonNullElseGet(
                request.getSession(true).getId(),
                () -> UUID.randomUUID().toString()
        );
        return "sess:" + httpSessionId;
    }

}
