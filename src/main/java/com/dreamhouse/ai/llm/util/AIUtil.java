package com.dreamhouse.ai.llm.util;

import com.dreamhouse.ai.authentication.exception.AuthenticatedUserNotFound;
import com.dreamhouse.ai.cloud.service.StorageService;
import com.dreamhouse.ai.llm.model.dto.HouseSearchDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility component with helper methods used across AI-related flows.
 */
@Component
public class AIUtil {

    private static final Pattern BED_PATTERN =
            Pattern.compile("\\b(\\d+)\\s*-?\\s*bed(room)?s?\\b");

    private static final Pattern MESSAGE_JSON_PATTERN =
            Pattern.compile("\"message\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);

    private static final ObjectMapper MAPPER = new ObjectMapper();


    private final StorageService storageService;


    public AIUtil(StorageService storageService) {
        this.storageService = storageService;
    }

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
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return "user:" + principal.getName();
        }
        String httpSessionId = Optional.ofNullable(request.getSession(false))
                .map(HttpSession::getId)
                .orElseGet(() -> UUID.randomUUID().toString());
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

    public HouseSearchDTO setImageViewUrls(@NotNull HouseSearchDTO reply) {
        for (var houseAdDTO : reply.getHouseAdDTOs()) {
            var houseAdImageDTOs = houseAdDTO.getImages()
                    .stream()
                    .distinct()
                    .map(image -> {
                        if (image.getStorageKey() != null && !image.getStorageKey().isEmpty()) {
                            image.setViewUrl(storageService
                                    .presignedGetUrl(image.getStorageKey(), Duration.ofDays(7))
                                    .orElse("undefined"));
                        }
                        return image;
                    })
                    .toList();
            houseAdDTO.setImages(houseAdImageDTOs);
        }
        return reply;
    }


    }

