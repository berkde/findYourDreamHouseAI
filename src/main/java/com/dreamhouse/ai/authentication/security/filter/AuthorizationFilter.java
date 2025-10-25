package com.dreamhouse.ai.authentication.security.filter;

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
    private static final String JWT_TOKEN_HEADER_PREFIX = "Bearer ";
    private static final String JWT_CONTENT_TYPE = "application/json";
    private static final String JWT_TOKEN_CLAIMS_KEY = "Authorities";
    private static final String AUTH_API_LOGIN_ENDPOINT = "/login";
    private static final String AUTH_API_REGISTER_ENDPOINT = "/api/v1/auth/register";
    private static final String HOUSE_ADS_API_GET_ENDPOINT = "/api/v1/houseAds";
    private static final String HTTP_OPTIONS_HEADER = "OPTIONS";
    private static final String HTTP_GET_HEADER = "GET";
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
        if (HTTP_OPTIONS_HEADER.equalsIgnoreCase(request.getMethod()) ||
                requestURI.equals(AUTH_API_LOGIN_ENDPOINT) ||
                requestURI.equals(AUTH_API_REGISTER_ENDPOINT) ||
                (requestURI.equals(HOUSE_ADS_API_GET_ENDPOINT) && request.getMethod().equals(HTTP_GET_HEADER))) {
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

        token = token.substring(JWT_TOKEN_HEADER_PREFIX.length());


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
                response.setContentType(JWT_CONTENT_TYPE);
                response.getWriter().write("{\"error\":\"invalid_token_subject\"}");
                return;
            }

            Collection<SimpleGrantedAuthority> authorities = Collections.emptyList();
            Object rawAuthorities = claims.get(JWT_TOKEN_CLAIMS_KEY);
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