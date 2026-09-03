package com.vst.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a business rule (not a simple field-level {@code @Valid}
 * constraint) is violated. Maps to 400 Bad Request.
 */
public class ValidationException extends AppException {

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }
}
