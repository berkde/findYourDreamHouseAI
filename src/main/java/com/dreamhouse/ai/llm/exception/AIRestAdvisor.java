package com.dreamhouse.ai.llm.exception;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AIRestAdvisor {

    private static final Logger log = LoggerFactory.getLogger(AIRestAdvisor.class);

    @ExceptionHandler(AITokenInvalidException.class)
    public ResponseEntity<?> handleAITokenInvalidException(@NotNull AITokenInvalidException e) {
        log.warn("AITokenInvalidException caught {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(AITokenNotFoundException.class)
    public ResponseEntity<?> handleAITokenNotFoundException(@NotNull AITokenNotFoundException e) {
        log.warn("AITokenNotFoundException caught {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(@NotNull BadRequestException e) {
        log.warn("BadRequestException caught {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(NoUserOrPlanCodeException.class)
    public ResponseEntity<?> handleNoUserOrPlanCodeException(@NotNull NoUserOrPlanCodeException e) {
        log.warn("NoUserOrPlanCodeException caught {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<?> handleQuotaExceededException(@NotNull QuotaExceededException e) {
        log.warn("QuotaExceededException caught {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(final Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError().body(ex.getMessage());
    }}
