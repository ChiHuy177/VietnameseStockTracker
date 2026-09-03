package com.vst.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsResourceNotFoundExceptionTo404() {
        ProblemDetail problem = handler.handleAppException(new ResourceNotFoundException("Stock", 42));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getProperties()).containsEntry("errorCode", "RESOURCE_NOT_FOUND");
        assertThat(problem.getDetail()).isEqualTo("Stock not found: id=42");
    }

    @Test
    void mapsConflictExceptionTo409() {
        ProblemDetail problem = handler.handleAppException(new ConflictException("Symbol already exists"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getProperties()).containsEntry("errorCode", "CONFLICT");
    }

    @Test
    void mapsValidationExceptionTo400() {
        ProblemDetail problem = handler.handleAppException(new ValidationException("Symbol must not be blank"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getProperties()).containsEntry("errorCode", "VALIDATION_ERROR");
    }

    @Test
    void mapsDataFetchExceptionTo502() {
        ProblemDetail problem = handler.handleAppException(new DataFetchException("vnstock API timed out"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(problem.getProperties()).containsEntry("errorCode", "DATA_FETCH_ERROR");
    }

    @Test
    void mapsNoResourceFoundExceptionTo404WithRouteNotFoundCode() {
        ProblemDetail problem = handler.handleNoResourceFound(new NoResourceFoundException(null, "/unknown"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getProperties()).containsEntry("errorCode", "ROUTE_NOT_FOUND");
    }

    @Test
    void mapsUnexpectedExceptionTo500() {
        ProblemDetail problem = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getProperties()).containsEntry("errorCode", "INTERNAL_ERROR");
    }

    @Test
    void everyResponseIncludesATimestamp() {
        ProblemDetail problem = handler.handleAppException(new ValidationException("bad input"));

        assertThat(problem.getProperties()).containsKey("timestamp");
    }
}
