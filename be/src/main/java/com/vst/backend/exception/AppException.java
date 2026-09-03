package com.vst.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for all application-specific exceptions. Carries the HTTP status
 * and a stable machine-readable {@code errorCode} that {@code GlobalExceptionHandler}
 * maps into every error response, so controllers/services never need to know
 * about HTTP concerns themselves and API clients can branch on the code
 * instead of parsing the human-readable message.
 */
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected AppException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    protected AppException(String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
