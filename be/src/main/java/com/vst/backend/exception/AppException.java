package com.vst.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for all application-specific exceptions. Carries the HTTP status
 * that {@code GlobalExceptionHandler} should map it to, so controllers/services
 * never need to know about HTTP concerns themselves.
 */
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;

    protected AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    protected AppException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
