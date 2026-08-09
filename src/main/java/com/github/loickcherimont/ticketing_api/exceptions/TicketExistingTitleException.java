package com.github.loickcherimont.ticketing_api.exceptions;

/**
 * Runtime child class used for existing ticket title.<br />
 * 
 * Converted to {@code HTTP 409 Conflict} by {@link GlobalExceptionHandler}.
 */
public class TicketExistingTitleException extends RuntimeException {

    public TicketExistingTitleException(String message) {
        super(message);
    }

}
