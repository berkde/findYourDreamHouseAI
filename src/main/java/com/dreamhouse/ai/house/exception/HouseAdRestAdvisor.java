package com.dreamhouse.ai.house.exception;

import com.dreamhouse.ai.authentication.exception.UserIDNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(name = "HouseAdRestAdvisor")
public class HouseAdRestAdvisor {
    private static final Logger log = LoggerFactory.getLogger(HouseAdRestAdvisor.class);

    @ExceptionHandler(UnsupportedContentException.class)
    public ResponseEntity<String> handleUnsupportedContentException(final UnsupportedContentException ex) {
        log.warn("Unsupported content exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }


    @ExceptionHandler(NoFilesException.class)
    public ResponseEntity<String> handleNoFilesException(final NoFilesException ex) {
        log.warn("No files exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }


    @ExceptionHandler(UserIDNotFoundException.class)
    public ResponseEntity<String> handleUserIDNotFoundException(final UserIDNotFoundException ex) {
        log.warn("User ID not found exception: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<String> handleEmptyFileException(final EmptyFileException ex) {
        log.warn("Empty file exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(CloudException.class)
    public ResponseEntity<String> handleCloudException(final CloudException ex) {
        log.warn("Cloud exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(HouseAdCreationException.class)
    public ResponseEntity<String> handleHouseAdCreationException(final HouseAdCreationException ex) {
        log.warn("House ad creation exception: {}", ex.getMessage());
        return ResponseEntity.internalServerError().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(HouseAdNotFoundException.class)
    public ResponseEntity<String> handleHouseAdNotFoundException(final HouseAdNotFoundException ex) {
        log.warn("House ad not found exception: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(HouseAdImageNotFoundException.class)
    public ResponseEntity<String> handleHouseAdImageNotFoundException(final HouseAdImageNotFoundException ex) {
        log.warn("House ad image not found exception: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(HouseAdMessageException.class)
    public ResponseEntity<String> handleHouseAdMessageException(final HouseAdMessageException ex) {
        log.warn("House ad message exception: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(HouseAdTitleAndDescriptionUpdateException.class)
    public ResponseEntity<String> handleHouseAdTitleAndDescriptionUpdateException(final HouseAdTitleAndDescriptionUpdateException ex) {
        log.warn("House ad title and description update exception: {}", ex.getMessage());
        return ResponseEntity.internalServerError().body("Reason: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(final Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError().body("Reason: " + ex.getMessage());
    }
}
