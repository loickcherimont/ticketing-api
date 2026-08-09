package com.github.loickcherimont.ticketing_api.exceptions;

/**
 * Runtime child class used for not found ticket identifier.<br />
 * 
 * Converted to {@code HTTP 404 Not Found} by {@link GlobalExceptionHandler}.
 */
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String message) {
        super(message);
    }

}
