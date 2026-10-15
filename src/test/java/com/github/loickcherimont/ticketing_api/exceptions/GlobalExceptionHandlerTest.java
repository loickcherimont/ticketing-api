package com.github.loickcherimont.ticketing_api.exceptions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tests for {@link GlobalExceptionHandler}.
 *
 * <p>
 * A {@code @RestControllerAdvice} is the Spring equivalent of an Express
 * error middleware ({@code app.use((err, req, res, next) => ...)}): it
 * catches exceptions thrown by any controller and converts them into
 * HTTP responses with a consistent JSON body ({@code ApiError}).
 * </p>
 *
 * <p>
 * Method: {@code MockMvcBuilders.standaloneSetup} builds a tiny throwaway
 * MVC stack (no Spring context, no database, no security) around a probe
 * controller. Each endpoint of the probe throws one specific exception so
 * we can assert the status code + JSON body the advice produces.
 * </p>
 */
class GlobalExceptionHandlerTest {

    /**
     * Minimal controller whose only purpose is to throw one exception per
     * endpoint so the advice can catch it. Like a test double for HTTP.
     */
    @RestController
    static class ProbeController {

        @PostMapping("/probe/validation")
        public void validation(@Valid @RequestBody ProbeDto dto) {
            // never reached: @Valid throws MethodArgumentNotValidException first
        }

        @GetMapping("/probe/not-found")
        public void notFound() {
            throw new TicketNotFoundException("Ticket introuvable");
        }

        @GetMapping("/probe/invalid-credentials")
        public void invalidCredentials() {
            throw new InvalidCredentialsException("Identifiants invalides");
        }

        @GetMapping("/probe/conflict")
        public void conflict() {
            throw new TicketExistingTitleException("Un ticket avec ce titre existe déjà");
        }
    }

    record ProbeDto(@NotBlank(message = "ne doit pas être vide") String title) {
    }

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // -------------------------------------------------------------------------
    // Tests — 400 Bad Request (validation)
    // -------------------------------------------------------------------------

    /**
     * When a {@code @Valid} body fails validation, Spring throws
     * {@link org.springframework.web.bind.MethodArgumentNotValidException}.
     * The advice answers 400 with a map of field → message so the client
     * knows what to fix (messages in French per project rules).
     */
    @Test
    @DisplayName("POST /probe/validation: should return HTTP 400 with field errors")
    void validation_withBlankField_returns400() throws Exception {

        mockMvc.perform(post("/probe/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProbeDto(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("La requête n'est pas valide"))
                .andExpect(jsonPath("$.fieldErrors.title").value("ne doit pas être vide"));
    }

    // -------------------------------------------------------------------------
    // Tests — 404 Not Found
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /probe/not-found: should return HTTP 404 with the exception message")
    void notFound_returns404() throws Exception {

        mockMvc.perform(get("/probe/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ticket introuvable"));
    }

    // -------------------------------------------------------------------------
    // Tests — 401 Unauthorized
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /probe/invalid-credentials: should return HTTP 401 with the exception message")
    void invalidCredentials_returns401() throws Exception {

        mockMvc.perform(get("/probe/invalid-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Identifiants invalides"));
    }

    // -------------------------------------------------------------------------
    // Tests — 409 Conflict
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /probe/conflict: should return HTTP 409 with the exception message")
    void conflict_returns409() throws Exception {

        mockMvc.perform(get("/probe/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Un ticket avec ce titre existe déjà"));
    }
}
