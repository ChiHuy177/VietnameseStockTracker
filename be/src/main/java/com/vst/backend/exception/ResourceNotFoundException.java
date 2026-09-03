package com.vst.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource (entity, external record) does not exist.
 * Maps to 404 Not Found.
 */
public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        this(resourceName + " not found: id=" + id);
    }
}
