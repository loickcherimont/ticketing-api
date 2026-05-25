package com.github.loickcherimont.ticketing_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Runtime child class used for existing ticket title.<br />
 * 
 * We use {@code @ResponseStatus} to transform the exception into {@code HTTP 409 Conflict}
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TicketExistingTitleException extends RuntimeException {

    public TicketExistingTitleException(String message) {
        super(message);
    }

}
