package com.github.loickcherimont.ticketing_api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.loickcherimont.ticketing_api.dto.SigninRequestDto;
import com.github.loickcherimont.ticketing_api.dto.SigninResponseDto;
import com.github.loickcherimont.ticketing_api.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for authentication endpoints.
 *
 * <p>
 * <strong>Responsibility:</strong> Handles HTTP requests related to user login/authentication.
 * Currently implements only sign-in (authentication).
 * </p>
 *
 * @see AuthService
 * @see com.github.loickcherimont.ticketing_api.filter.JwtAuthenticationFilter
 */
@Tag(name = "Authentication", description = "User authentication and JWT token management")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates a user and returns a JWT token.
     *
     *
     * @param signinRequestDto DTO containing email and password from client
     *                        Automatically validated by Spring (@Valid annotation)
     * @return ResponseEntity with HTTP 200 OK and SigninResponseDto body containing JWT token
     * @throws com.github.loickcherimont.ticketing_api.exceptions.InvalidCredentialsException
     *         if email not found or password incorrect (converted to HTTP 401 by {@link com.github.loickcherimont.ticketing_api.exceptions.GlobalExceptionHandler})
     *
     * @see SigninRequestDto
     * @see SigninResponseDto
     * @see AuthService#signin(SigninRequestDto)
     */
    @Operation(
        summary = "Authenticate user and get JWT token",
        description = "Validates email and password, returns JWT token for subsequent API requests if credentials are correct"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Authentication successful",
        content = @Content(schema = @Schema(implementation = SigninResponseDto.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Validation failed - blank or invalid email/password format",
        content = @Content()
    )
    @ApiResponse(
        responseCode = "401",
        description = "Email not found or password incorrect (generic message prevents user enumeration)",
        content = @Content()
    )
    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDto> signin(@Valid @RequestBody SigninRequestDto signinRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.signin(signinRequestDto));
    }
}
