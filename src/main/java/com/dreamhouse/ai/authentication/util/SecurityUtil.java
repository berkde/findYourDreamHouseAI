package com.dreamhouse.ai.authentication.util;

import com.dreamhouse.ai.authentication.exception.AuthenticatedUserNotFound;
import com.dreamhouse.ai.authentication.exception.UserIDNotFoundException;
import com.dreamhouse.ai.authentication.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class SecurityUtil {
    private static final Logger log = LoggerFactory.getLogger(SecurityUtil.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private final UserRepository userRepository;
    private final SecretKey secretKey;


    @Autowired
    public SecurityUtil(UserRepository userRepository,
                        SecretKey secretKey) {
        this.userRepository = userRepository;
        this.secretKey = secretKey;
    }

    public boolean isTokenValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return false;
        final String token = rawToken.replaceFirst("^Bearer\\s+", "");

        try {
            Jwts.parser()
                    .decryptWith(secretKey)
                    .clockSkewSeconds(60)
                    .build()
                    .parseEncryptedClaims(token);
            return true;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.info("Token expired at {}", e.getClaims().getExpiration());
            return false;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }



    public Boolean isUserRequestValid(String queriedUserId, String username) {
        log.info("getUserProfile - Authentication principal: {}", username);

        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Boolean.FALSE;

        String token = attrs.getRequest().getHeader(AUTHORIZATION_HEADER);

        if (!isUserTokenValid(token, queriedUserId, username)) {
            log.error("Invalid token");
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }


    private Boolean isUserTokenValid(String token, String queriedUserId, String username) {
        var user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var userQueried = userRepository.findByUserID(queriedUserId).orElseThrow(() -> new UserIDNotFoundException("User not found"));

        if (user.getAuthorizationToken() == null ||
                !user.getAuthorizationToken().equals(token) ||
                !user.getAuthorizationToken().equals(userQueried.getAuthorizationToken())) {
            log.error("Invalid token");
            return Boolean.FALSE;
        }

        token = token.substring(BEARER_TOKEN_PREFIX.length());

        // Do not log token contents

        if (!StringUtils.hasText(token)) {
            log.error("Empty authorization header");
            return Boolean.FALSE;
        }

        try {
            var claims = Jwts.parser()
                    .decryptWith(secretKey)
                    .build()
                    .parseEncryptedClaims(token)
                    .getPayload();


            String subject = claims.getSubject();
            if (!StringUtils.hasText(subject) || !subject.equals(username)) {
                log.error("Invalid token subject");
                return Boolean.FALSE;
            }

            if (claims.getExpiration().before(Date.from(Instant.now()))) {
                log.error("Token expired");
                return Boolean.FALSE;
            }

        return Boolean.TRUE;
    } catch (Exception e) {
        log.error("{}", e.getMessage());
        return Boolean.FALSE;
        }
    }

    /**
     * Returns the username of the currently authenticated user from Spring Security's context.
     * <p>
     * Reads the current Authentication from SecurityContextHolder and returns its name. If there is no
     * authenticated principal (for example, missing/anonymous authentication), this method throws a
     * runtime exception indicating authentication failed.
     * </p>
     *
     * @return the non-blank username of the current authenticated principal
     */
    public String getAuthenticatedUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null || username.isBlank()) {
            throw new AuthenticatedUserNotFound("Authentication failed");
        }
        return username;
    }


}
