package com.github.loickcherimont.ticketing_api.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.loickcherimont.ticketing_api.dto.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 
     * @param ex Exception
     * @return ResponseEntity containing fields with each error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "La requête n'est pas valide", errors);
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ApiError> handleTicketNotFound(TicketNotFoundException ex) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(TicketExistingTitleException.class)
    public ResponseEntity<ApiError> handleTicketExistingTitle(TicketExistingTitleException ex) {

        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus httpStatus, String message,
            Map<String, String> fieldErrors) {

        ApiError apiError = new ApiError(
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                fieldErrors);

        return ResponseEntity.status(httpStatus).body(apiError);
    }
}