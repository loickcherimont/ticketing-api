package com.github.loickcherimont.ticketing_api.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loickcherimont.ticketing_api.models.Role;
import com.github.loickcherimont.ticketing_api.models.User;
import com.github.loickcherimont.ticketing_api.services.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Unit tests for {@link JwtAuthenticationFilter}.
 *
 * <p>
 * A servlet filter is the Spring equivalent of Express/Koa middleware: it
 * intercepts every request <em>before</em> it reaches the controllers.
 * This filter's job is to read the {@code Authorization: Bearer <token>}
 * header, verify the JWT and store the authenticated user in the
 * {@link SecurityContextHolder} (Spring's request-scoped "session" for
 * security).
 * </p>
 *
 * <p>
 * Everything is mocked: no Spring context, no database, no real HTTP
 * server. We only simulate the three objects the filter talks to:
 * the request, the response and the filter chain.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String EMAIL = "sample@email.com";
    private static final String TOKEN = "fake.jwt.token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private JwtAuthenticationFilter filter;

    private final StringWriter responseBody = new StringWriter();

    @BeforeEach
    void setUp() {
        // The filter must be built AFTER Mockito has injected the mocks
        // (field initializers run before the extension injects @Mock fields)
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService, objectMapper);
        // Start each test with a clean security context (like a fresh HTTP request)
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Never leak the authenticated user from one test to the next
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // Tests — missing or non-Bearer Authorization header
    // -------------------------------------------------------------------------

    /**
     * No {@code Authorization} header at all: the filter must not try to
     * authenticate — it simply lets the request continue down the chain.
     * Spring Security itself will then reject protected endpoints with 401.
     */
    @Test
    @DisplayName("doFilterInternal: without Authorization header, should pass the request through")
    void doFilterInternal_withoutAuthorizationHeader_passesThrough() throws ServletException, Exception {

        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * An {@code Authorization} header without the {@code Bearer } prefix
     * (e.g. {@code Basic ...}) is not a JWT for us: same behaviour,
     * pass through untouched.
     */
    @Test
    @DisplayName("doFilterInternal: with a non-Bearer header, should pass the request through")
    void doFilterInternal_withNonBearerHeader_passesThrough() throws ServletException, Exception {

        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // -------------------------------------------------------------------------
    // Tests — valid token
    // -------------------------------------------------------------------------

    /**
     * Happy path: valid Bearer token → the filter loads the user, checks
     * the token and puts a {@code UsernamePasswordAuthenticationToken}
     * into the {@link SecurityContextHolder}. Downstream code (controllers
     * with {@code @AuthenticationPrincipal}, role checks…) can then see
     * "who is calling".
     */
    @Test
    @DisplayName("doFilterInternal: with a valid token, should populate the SecurityContextHolder")
    void doFilterInternal_withValidToken_authenticates() throws ServletException, Exception {

        User user = new User(null, EMAIL, "password", Role.USER);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(user);
        when(jwtService.isTokenValid(TOKEN, user)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(user);
        // The request must still continue to the controller
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Token signature/claims are valid but the token itself is expired:
     * {@code isTokenValid} returns {@code false}, so the filter must NOT
     * authenticate the request. The chain still continues, and Spring
     * Security answers 401 on protected routes.
     */
    @Test
    @DisplayName("doFilterInternal: with an expired token, should not populate the SecurityContextHolder")
    void doFilterInternal_withExpiredToken_doesNotAuthenticate() throws ServletException, Exception {

        User user = new User(null, EMAIL, "password", Role.USER);
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(user);
        when(jwtService.isTokenValid(TOKEN, user)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // -------------------------------------------------------------------------
    // Tests — invalid token (JwtException)
    // -------------------------------------------------------------------------

    /**
     * Malformed or badly signed token: {@code extractUsername} throws a
     * {@link JwtException}. The filter catches it and writes a 401 JSON
     * response itself — it does NOT continue the chain, so the controller
     * is never reached.
     */
    @Test
    @DisplayName("doFilterInternal: with an invalid token, should respond 401 and stop the chain")
    void doFilterInternal_withInvalidToken_responds401() throws ServletException, Exception {

        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenThrow(new JwtException("Malformed JWT"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/tickets");
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Chain is NOT continued: the request never reaches the controller
        verify(filterChain, never()).doFilter(any(), any());
        // The body must contain the French error message (client-visible)
        assertThat(responseBody.toString()).contains("Token invalide ou expiré");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // -------------------------------------------------------------------------
    // Tests — user not found
    // -------------------------------------------------------------------------

    /**
     * The token is syntactically valid and carries an email, but no user
     * with that email exists anymore (deleted account). Loading the user
     * throws an {@link org.springframework.security.core.AuthenticationException}
     * — same 401 response as above.
     */
    @Test
    @DisplayName("doFilterInternal: when the user no longer exists, should respond 401")
    void doFilterInternal_withUnknownUser_responds401() throws ServletException, Exception {

        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(BEARER_PREFIX + TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/api/tickets");
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
        assertThat(responseBody.toString()).contains("Token invalide ou expiré");
    }
}
