package com.github.loickcherimont.ticketing_api.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loickcherimont.ticketing_api.controllers.AuthController;
import com.github.loickcherimont.ticketing_api.controllers.TicketController;
import com.github.loickcherimont.ticketing_api.dto.SigninRequestDto;
import com.github.loickcherimont.ticketing_api.dto.SigninResponseDto;
import com.github.loickcherimont.ticketing_api.dto.SolutionRequestDto;
import com.github.loickcherimont.ticketing_api.dto.TicketResponseDto;
import com.github.loickcherimont.ticketing_api.filter.JwtAuthenticationFilter;
import com.github.loickcherimont.ticketing_api.models.Role;
import com.github.loickcherimont.ticketing_api.models.TicketStatus;
import com.github.loickcherimont.ticketing_api.models.User;
import com.github.loickcherimont.ticketing_api.services.AuthService;
import com.github.loickcherimont.ticketing_api.services.JwtService;
import com.github.loickcherimont.ticketing_api.services.TicketService;

/**
 * Security tests for {@link SecurityConfig}.
 *
 * <p>
 * Unlike {@code TicketControllerTest} (which stubs the JWT filter to focus
 * on controller logic), this test keeps the REAL
 * {@link JwtAuthenticationFilter} in the chain: it validates the
 * authorization rules declared in {@link SecurityConfig#filterChain}
 * end-to-end within the MVC slice.
 * </p>
 *
 * <p>
 * Four rules are checked:
 * <ul>
 * <li>public endpoints ({@code /api/auth/**}) answer without authentication</li>
 * <li>protected endpoints answer 401 when no token is sent</li>
 * <li>role-protected endpoints answer 403 when the role is missing</li>
 * <li>role-protected endpoints let an AGENT through</li>
 * </ul>
 * </p>
 */
@WebMvcTest(controllers = { TicketController.class, AuthController.class })
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class SecurityConfigTest {

    private static final UUID TICKET_ID = UUID.randomUUID();
    private static final String SOLUTION = "Remboursement effectué le 15 octobre.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Services called by the two controllers (mocked: no DB in a MVC slice)
    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private AuthService authService;

    // Injected into the REAL JwtAuthenticationFilter imported above
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // -------------------------------------------------------------------------
    // Tests — public endpoints (permitAll)
    // -------------------------------------------------------------------------

    /**
     * {@code /api/auth/**} is declared {@code permitAll} in SecurityConfig:
     * it must be reachable WITHOUT any token. We only assert "not 401" —
     * the controller itself is mocked, so a 200 proves security let the
     * request through.
     */
    @Test
    @DisplayName("POST /api/auth/signin: should be accessible without authentication")
    void signin_withoutAuthentication_isAccessible() throws Exception {

        when(authService.signin(any(SigninRequestDto.class)))
                .thenReturn(new SigninResponseDto("token", "user@banque.fr", Role.USER));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SigninRequestDto("user@banque.fr", "password"))))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // Tests — protected endpoints (authenticated)
    // -------------------------------------------------------------------------

    /**
     * No token at all on a protected endpoint: the real filter finds no
     * Authorization header and passes through, then Spring Security's
     * entry point rejects the request with 401 (see
     * {@code exceptionHandling} in SecurityConfig).
     */
    @Test
    @DisplayName("GET /api/tickets: should return HTTP 401 without a token")
    void getAllTickets_withoutToken_returns401() throws Exception {

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // Tests — role-protected endpoints (ROLE_AGENT)
    // -------------------------------------------------------------------------

    /**
     * An authenticated USER (not AGENT) tries an AGENT-only endpoint:
     * security must answer 403 Forbidden. {@code @WithMockUser(roles="USER")}
     * pre-fills the SecurityContext, exactly like a valid JWT would.
     */
    @Test
    @DisplayName("PATCH /api/tickets/{id}/solve: should return HTTP 403 for a USER")
    void solveTicket_asUser_returns403() throws Exception {

        mockMvc.perform(patch("/api/tickets/{id}/solve", TICKET_ID)
                        .with(user("user@banque.fr").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SolutionRequestDto(SOLUTION))))
                .andExpect(status().isForbidden());
    }

    /**
     * Same endpoint with the AGENT role: security must let the request
     * through (no 401/403). The mocked service returns a ticket so we
     * assert 200 — proving the request reached the controller.
     */
    @Test
    @DisplayName("PATCH /api/tickets/{id}/solve: should pass security for an AGENT")
    void solveTicket_asAgent_passesSecurity() throws Exception {

        User agent = new User(UUID.randomUUID(), "agent@banque.fr", "password", Role.AGENT);
        TicketResponseDto solved = new TicketResponseDto(TICKET_ID, "Title", "Description",
                TicketStatus.CLOSED, SOLUTION, agent.getEmail(), agent.getEmail());
        when(ticketService.solveTicket(any(UUID.class), any(SolutionRequestDto.class))).thenReturn(solved);

        mockMvc.perform(patch("/api/tickets/{id}/solve", TICKET_ID)
                        .with(user("agent@banque.fr").roles("AGENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SolutionRequestDto(SOLUTION))))
                .andExpect(status().isOk());
    }
}
