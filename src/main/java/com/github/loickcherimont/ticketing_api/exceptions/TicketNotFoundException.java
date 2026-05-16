package com.github.loickcherimont.ticketing_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Runtime child class used for not found ticket identifier.<br />
 * 
 * We use {@code @ResponseStatus} to transform the exception into {@code HTTP 404 NOT FOUND status}
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(String message) {
        super(message);
    }

}
