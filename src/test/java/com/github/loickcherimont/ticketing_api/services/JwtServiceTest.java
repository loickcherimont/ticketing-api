package com.github.loickcherimont.ticketing_api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.loickcherimont.ticketing_api.services.impl.JwtServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Unit tests for {@link JwtServiceImpl}.
 *
 * <p>The {@link UserDetails} dependency is mocked with Mockito:
 * no database is accessed during test execution.</p>
 *
 * <p>Covered scenarios:</p>
 * <ul>
 *   <li>Extracting the email stored as the token subject</li>
 *   <li>Generating a token that carries user details, extra claims and expiration dates</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    // -------------------------------------------------------------------------
    // Shared test data
    // -------------------------------------------------------------------------

    private static final String SECRET_KEY = "pr+O1zq3Jt2WGyLSzA65H6LOnno"
            + "+VrQFpNFekUyAlXQixp8vczlLKAeoAyTUpW9xkRAJjalZMv2D4ZcMvB4kOw==";
    private static final long EXPIRATION_TIME = 3_600_000L;
    private static final long CLOCK_TOLERANCE_MS = 2_000L;

    private static final String EMAIL = "sample@email.com";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String EXTRA_CLAIM_KEY = "foo";
    private static final String EXTRA_CLAIM_VALUE = "bar";

    // -------------------------------------------------------------------------
    // Mocks and class under test
    // -------------------------------------------------------------------------

    @Mock
    private UserDetails userDetails;

    private final JwtServiceImpl jwtService = new JwtServiceImpl(SECRET_KEY, EXPIRATION_TIME);

    // -------------------------------------------------------------------------
    // Tests — extractUsername
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("extractUsername: should return the email stored as the token subject")
    void extractUsername_returnsEmailFromSubject() {

        when(userDetails.getUsername()).thenReturn(EMAIL);
        String token = jwtService.generateToken(userDetails);

        String actualEmail = jwtService.extractUsername(token);

        assertThat(actualEmail).isEqualTo(EMAIL);
        verify(userDetails).getUsername();
    }

    // -------------------------------------------------------------------------
    // Tests — generateToken
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generateToken: should carry user details, extra claims and expiration dates")
    void generateToken_carriesUserDetailsAndClaims() {

        List<GrantedAuthority> authorities = List.of(
                (GrantedAuthority) () -> ROLE_ADMIN,
                (GrantedAuthority) () -> ROLE_USER);
        when(userDetails.getUsername()).thenReturn(EMAIL);
        doReturn(authorities).when(userDetails).getAuthorities();

        String token = jwtService.generateToken(Map.of(EXTRA_CLAIM_KEY, EXTRA_CLAIM_VALUE), userDetails);

        String subject = jwtService.extractClaim(token, Claims::getSubject);
        String extraClaim = jwtService.extractClaim(token, claims -> claims.get(EXTRA_CLAIM_KEY, String.class));
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) jwtService.extractClaim(token, claims -> claims.get("roles"));
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
        Instant now = Instant.now();

        assertThat(token).isNotBlank();
        assertThat(subject).isEqualTo(EMAIL);
        assertThat(extraClaim).isEqualTo(EXTRA_CLAIM_VALUE);
        assertThat(roles).containsExactly(ROLE_ADMIN, ROLE_USER);
        assertThat(issuedAt).isCloseTo(Date.from(now), CLOCK_TOLERANCE_MS);
        assertThat(expiration).isCloseTo(Date.from(now.plusMillis(EXPIRATION_TIME)), CLOCK_TOLERANCE_MS);
    }

    // -------------------------------------------------------------------------
    // Tests — token signature
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("token signature: should carry a valid signature")
    void token_withValidSignature_parsesSuccessfully() {
        when(userDetails.getUsername()).thenReturn(EMAIL);
        String token = jwtService.generateToken(userDetails);

        // Parse with the signing key - throw if signature invalid
        Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY))).build().parseClaimsJws(token).getBody();

        assertThat(token).isNotBlank();
        assertThat(claims.getSubject()).isEqualTo(EMAIL);
    }

    // -------------------------------------------------------------------------
    // Tests — token expiration
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("token expiration: should expire after the configured delay")
    void token_expiresAfterConfiguredDelay() {
        when(userDetails.getUsername()).thenReturn(EMAIL);
        String token = jwtService.generateToken(userDetails);

        // Parsing succeeds → the token is not expired yet
        Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY))).build().parseClaimsJws(token).getBody();
        Date expiration = claims.getExpiration();

        assertThat(expiration).isAfter(Date.from(Instant.now()));
        assertThat(expiration).isCloseTo(Date.from(Instant.now().plusMillis(EXPIRATION_TIME)), CLOCK_TOLERANCE_MS);
    }

    // -------------------------------------------------------------------------
    // Tests — tampered token
    // -------------------------------------------------------------------------

    /**
     * A "tampered" token is a token whose signature has been altered after
     * generation.
     *
     * <p>
     * Why test this? The whole point of a JWT signature is integrity: if
     * anyone changes the token without the secret key, verification MUST
     * fail. This is the negative counterpart of the valid-signature test
     * above.
     * </p>
     *
     * <p>
     * How it works: a JWT looks like {@code header.payload.signature}
     * (three Base64URL segments separated by dots). We flip one character
     * of the last segment (the signature) so the receiver recomputes a
     * different signature and detects the mismatch.
     * </p>
     *
     * <p>
     * Note: {@code extractUsername} returns {@code null} instead of
     * throwing, because {@code JwtServiceImpl.extractAllClaims} catches
     * every exception and returns {@code null}.
     * </p>
     */
    @Test
    @DisplayName("tampered token: should be rejected when the signature has been altered")
    void token_withTamperedSignature_isRejected() {

        when(userDetails.getUsername()).thenReturn(EMAIL);
        String token = jwtService.generateToken(userDetails);

        // Corrupt the last character of the signature segment (3rd part)
        String[] parts = token.split("\\.");
        String signature = parts[2];
        String flipped = signature.endsWith("A") ? "B" : "A";
        String tamperedToken = parts[0] + "." + parts[1] + "." + signature.substring(0, signature.length() - 1) + flipped;

        // Parsing fails → extractAllClaims swallows the exception → null
        assertThat(jwtService.extractUsername(tamperedToken)).isNull();
        assertThat(jwtService.isTokenValid(tamperedToken, userDetails)).isFalse();
    }
}
