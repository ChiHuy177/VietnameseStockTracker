package com.vst.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when fetching data from an external source (market data API,
 * downstream service) fails. Maps to 502 Bad Gateway since the failure
 * originates outside this application.
 */
public class DataFetchException extends AppException {

    public DataFetchException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }

    public DataFetchException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_GATEWAY, cause);
    }
}
