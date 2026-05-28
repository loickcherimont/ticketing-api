package com.github.loickcherimont.ticketing_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Runtime child class used for wrong login credentials.<br />
 * 
 * We use {@code @ResponseStatus} to transform the exception into {@code HTTP 400 Bad Request}
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
