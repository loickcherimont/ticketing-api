package com.github.loickcherimont.ticketing_api.exceptions;

/**
 * Runtime child class used for wrong login credentials.<br />
 * 
 * Converted to {@code HTTP 401 Unauthorized} by {@link GlobalExceptionHandler}.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
