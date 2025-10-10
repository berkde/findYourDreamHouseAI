package com.dreamhouse.ai.authentication.configuration;

import com.dreamhouse.ai.authentication.dto.UserDTO;
import com.dreamhouse.ai.authentication.model.request.LoginRequestModel;
import com.dreamhouse.ai.authentication.repository.UserRepository;
import com.dreamhouse.ai.authentication.service.impl.UserServiceImpl;
import com.dreamhouse.ai.authentication.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final UserServiceImpl userService;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final SecretKey key;

    private static final long TOKEN_EXPIRATION = 3600000L;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UserServiceImpl userService,
                                UserRepository userRepository,
                                SecurityUtil securityUtil,
                                SecretKey key) {
        super.setAuthenticationManager(authenticationManager);
        this.userService = userService;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.key = key;
        setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            var credentials = new ObjectMapper()
                    .readValue(request.getInputStream(), LoginRequestModel.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.username(),
                            credentials.password(),
                            new ArrayList<>()
                    )
            );

        } catch (Exception e) {
            logger.error("Error parsing login request", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) {
        String username = authResult.getName();
        UserDTO userDTO = userService.getUserByUsername(username);

        if (userDTO == null) {
            logger.error("User not found: {}", username);
            throw new RuntimeException("User not found");
        }

        var authorities = authResult.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Always produce a token value with Bearer prefix for responses and persistence
        String tokenWithPrefix;

        if (user.getAuthorizationToken() != null && securityUtil.isTokenValid(user.getAuthorizationToken())) {
            // Re-use existing valid token as-is (already includes Bearer prefix)
            tokenWithPrefix = user.getAuthorizationToken();
        } else {
            // Create a new raw token and persist with Bearer prefix
            String rawToken = Jwts.builder()
                    .subject(username)
                    .claim("authorities", authorities)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                    .encryptWith(key, Jwts.ENC.A256GCM)
                    .compact();
            tokenWithPrefix = "Bearer " + rawToken;
            user.setAuthorizationToken(tokenWithPrefix);
        }

        user.setLastLogin(new Date());
        userRepository.save(user);

        // Set the correct Authorization header without duplicating Bearer
        response.addHeader("Authorization", tokenWithPrefix);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        try {
            String responseBody = String.format(
                "{\"message\":\"Login successful\",\"username\":\"%s\",\"token\":\"%s\"}", 
                username, tokenWithPrefix
            );
            response.getWriter().write(responseBody);
        } catch (Exception e) {
            logger.error("Error writing response body", e);
        }

    }
}
