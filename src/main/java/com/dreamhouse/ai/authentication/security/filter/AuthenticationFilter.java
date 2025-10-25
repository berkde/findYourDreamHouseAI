package com.dreamhouse.ai.authentication.security.filter;

import com.dreamhouse.ai.authentication.exception.AuthenticationFailedException;
import com.dreamhouse.ai.authentication.model.request.LoginRequestModel;
import com.dreamhouse.ai.authentication.repository.UserRepository;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String JWT_TOKEN_HEADER_PREFIX = "Bearer ";
    private static final String JWT_TOKEN_CLAIMS_KEY = "Authorities";
    private static final String JWT_AUTHORIZATION_HEADER = "Authorization";
    private static final String JWT_CONTENT_TYPE = "application/json";
    private static final String AUTHENTICATION_FILTER_DEFAULT_LOGIN_URL = "/login";
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final SecretKey key;

    private static final long TOKEN_EXPIRATION = 3600000L;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UserRepository userRepository,
                                SecurityUtil securityUtil,
                                SecretKey key) {
        super.setAuthenticationManager(authenticationManager);
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.key = key;
        setFilterProcessesUrl(AUTHENTICATION_FILTER_DEFAULT_LOGIN_URL);
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
        } catch (IOException e) {
            logger.error("Failed to read login request body, details: {}", e.getMessage());
            throw new AuthenticationFailedException("Failed to read login request body");
        } catch (Exception e) {
            logger.error("Failed to authenticate user, details: {}", e.getMessage());
            throw new AuthenticationFailedException("Failed to authenticate user");
        }


    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) {
        try {
            String username = authResult.getName();

            var user = userRepository
                    .findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            var authorities = authResult.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String tokenWithPrefix;

            if (user.getAuthorizationToken() != null && securityUtil.isTokenValid(user.getAuthorizationToken())) {
                tokenWithPrefix = user.getAuthorizationToken();
            } else {
                String rawToken = Jwts.builder()
                        .subject(username)
                        .claim(JWT_TOKEN_CLAIMS_KEY, authorities)
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                        .encryptWith(key, Jwts.ENC.A256CBC_HS512)
                        .compact();
                tokenWithPrefix = JWT_TOKEN_HEADER_PREFIX + rawToken;
                user.setAuthorizationToken(tokenWithPrefix);
            }

            user.setLastLogin(new Date());
            userRepository.save(user);

            response.addHeader(JWT_AUTHORIZATION_HEADER, tokenWithPrefix);
            response.setContentType(JWT_CONTENT_TYPE);
            response.setStatus(HttpServletResponse.SC_OK);


            String responseBody = String.format(
                    "{\"message\":\"Login successful\",\"username\":\"%s\",\"token\":\"%s\"}",
                    username, tokenWithPrefix
                );
            response.getWriter().write(responseBody);
        } catch (IOException e) {
            logger.error("{}", e.getMessage());
            throw new RuntimeException(e);
        } catch (UsernameNotFoundException e) {
            logger.error("User not found", e);
            throw new AuthenticationFailedException("User authentication failed because user not found");
        }


    }
}
