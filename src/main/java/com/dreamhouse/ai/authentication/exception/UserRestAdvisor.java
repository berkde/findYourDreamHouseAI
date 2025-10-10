package com.dreamhouse.ai.authentication.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserRestAdvisor {

    private static final Logger log = LoggerFactory.getLogger(UserRestAdvisor.class);

    @ExceptionHandler(AddressAddingException.class)
    public ResponseEntity<String> handleAddressAddingException(final AddressAddingException ex) {
        log.warn("Address adding exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(UserIDNotFoundException.class)
    public ResponseEntity<String> handleUserIDNotFoundException(final UserIDNotFoundException ex) {
        log.warn("User ID not found: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(AuthoritiesNotEditedException.class)
    public ResponseEntity<String> handleAuthoritiesNotEditedException(final AuthoritiesNotEditedException ex) {
        log.warn("Authorities not edited: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }


    @ExceptionHandler(AccountNotDeletedException.class)
    public ResponseEntity<String> handleAccountNotDeletedException(final AccountNotDeletedException ex) {
        log.warn("Account not deleted: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(UserAccountNotCreatedException.class)
    public ResponseEntity<String> handleUserAccountNotCreatedException(final UserAccountNotCreatedException ex) {
        log.warn("User account not created: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(final UserAlreadyExistsException ex) {
        log.warn("User already exists: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(final Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError().body("Reason: " + ex.getMessage());
    }
}
