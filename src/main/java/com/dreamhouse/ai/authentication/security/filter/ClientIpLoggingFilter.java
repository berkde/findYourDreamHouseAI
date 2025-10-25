package com.dreamhouse.ai.authentication.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class ClientIpLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ClientIpLoggingFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        String method = request.getMethod();
        String path = request.getRequestURI();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? String.valueOf(auth.getPrincipal()) : "anonymous";

        // Avoid logging static actuator or favicon noise if any appear in future; currently log all.
        log.info("HTTP {} {} from ip={} user={}", method, path, clientIp, username);

        filterChain.doFilter(request, response);
    }

    public static String resolveClientIp(HttpServletRequest request) {
        // RFC 7239 / common de-facto headers
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // XFF can be a comma-separated list. First one is original client.
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) return normalizeLoopback(first);
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return normalizeLoopback(xRealIp.trim());
        }
        return normalizeLoopback(request.getRemoteAddr());
    }

    private static String normalizeLoopback(String ip) {
        // Map IPv6 loopback to 127.0.0.1 for readability
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) return "127.0.0.1";
        return ip;
    }
}
