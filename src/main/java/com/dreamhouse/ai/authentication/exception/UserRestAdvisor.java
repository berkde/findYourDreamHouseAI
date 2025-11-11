package com.dreamhouse.ai.authentication.exception;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserRestAdvisor {

    private static final Logger log = LoggerFactory.getLogger(UserRestAdvisor.class);

    @ExceptionHandler(UserTypeNotFoundException.class)
    public ResponseEntity<?> handleUserTypeNotFoundException(UserTypeNotFoundException e) {
        log.warn("UserTypeNotFoundException {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(AddressAddingException.class)
    public ResponseEntity<String> handleAddressAddingException(@NotNull final AddressAddingException ex) {
        log.warn("Address adding exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(UserIDNotFoundException.class)
    public ResponseEntity<String> handleUserIDNotFoundException(@NotNull final UserIDNotFoundException ex) {
        log.warn("User ID not found: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(AuthoritiesNotEditedException.class)
    public ResponseEntity<String> handleAuthoritiesNotEditedException(@NotNull final AuthoritiesNotEditedException ex) {
        log.warn("Authorities not edited: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }


    @ExceptionHandler(AccountNotDeletedException.class)
    public ResponseEntity<String> handleAccountNotDeletedException(@NotNull final AccountNotDeletedException ex) {
        log.warn("Account not deleted: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(UserAccountNotCreatedException.class)
    public ResponseEntity<String> handleUserAccountNotCreatedException(@NotNull final UserAccountNotCreatedException ex) {
        log.warn("User account not created: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(SecurityKeyException.class)
    public ResponseEntity<String> handleSecurityKeyException(@NotNull final SecurityKeyException ex) {
        log.warn("Security key exception: {}", ex.getMessage());
        return ResponseEntity.internalServerError().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(UserAccountNotDeletedException.class)
    public ResponseEntity<String> handleUserAccountNotDeletedException(@NotNull final UserAccountNotDeletedException ex) {
        log.warn("User account not deleted: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(@NotNull final UserAlreadyExistsException ex) {
        log.warn("User already exists: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(DatabaseConnectionException.class)
    public ResponseEntity<String> handleDatabaseConnectionException(@NotNull final DatabaseConnectionException ex) {
        log.warn("Database connection error: {}", ex.getMessage());
        return ResponseEntity.internalServerError().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(@NotNull final MethodArgumentNotValidException ex) {
        log.warn("Method argument not valid: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(final Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError().body(ex.getMessage());
    }
}
