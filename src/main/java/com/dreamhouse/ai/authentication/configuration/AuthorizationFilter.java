package com.dreamhouse.ai.authentication.configuration;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class AuthorizationFilter extends BasicAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);
    private final SecretKey key;

    public AuthorizationFilter(AuthenticationManager authenticationManager, SecretKey key) {
        super(authenticationManager);
        this.key = key;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // Allow unauthenticated access to explicitly permitted endpoints only
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) ||
                requestURI.equals("/login") ||
                requestURI.equals("/api/v1/auth/register") ||
                (requestURI.equals("/api/v1/houseAds") && request.getMethod().equals("GET"))) {
            log.debug("Skipping authorization for permitted public endpoint or CORS preflight: {} {}", request.getMethod(), requestURI);
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            log.error("Missing or invalid authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        token = token.substring("Bearer ".length());

        // Do not log tokens to avoid leaking sensitive information

        if (!StringUtils.hasText(token)) {
            log.error("Empty authorization header");
            return;
        }

        try {
            var claims = Jwts.parser()
                    .decryptWith(key)
                    .build()
                    .parseEncryptedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            if (!StringUtils.hasText(username)) {
                log.error("Invalid token subject");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"invalid_token_subject\"}");
                return;
            }

            Collection<SimpleGrantedAuthority> authorities = Collections.emptyList();
            Object rawAuthorities = claims.get("authorities");
            if (rawAuthorities instanceof List<?> list) {
                authorities = list.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .map(SimpleGrantedAuthority::new)
                        .collect(toList());
            }

            var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info("Authentication success for user {}", username);
            chain.doFilter(request, response);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("{}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}