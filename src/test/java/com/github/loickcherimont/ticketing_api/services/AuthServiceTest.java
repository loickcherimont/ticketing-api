package com.github.loickcherimont.ticketing_api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.loickcherimont.ticketing_api.dto.SigninRequestDto;
import com.github.loickcherimont.ticketing_api.dto.SigninResponseDto;
import com.github.loickcherimont.ticketing_api.exceptions.InvalidCredentialsException;
import com.github.loickcherimont.ticketing_api.models.Role;
import com.github.loickcherimont.ticketing_api.models.User;
import com.github.loickcherimont.ticketing_api.repository.UserRepository;
import com.github.loickcherimont.ticketing_api.services.impl.AuthServiceImpl;

/**
 * Unit tests for {@link AuthServiceImpl}.
 *
 * <p>All dependencies (repository, JwtService, PasswordEncoder) are mocked with Mockito:
 * no database is accessed during test execution.</p>
 *
 * <p>Covered scenarios:</p>
 * <ul>
 *   <li>Signing in with valid credentials (USER and AGENT roles)</li>
 *   <li>Throwing {@link InvalidCredentialsException} for an unknown email</li>
 *   <li>Throwing {@link InvalidCredentialsException} for a wrong password</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // -------------------------------------------------------------------------
    // Shared test data
    // Centralized here: if a value changes, it only needs to be updated once.
    // -------------------------------------------------------------------------

    private static final UUID   ID       = UUID.randomUUID();
    private static final String EMAIL    = "user@gmail.com";
    private static final String PASSWORD = "test789";
    private static final String TOKEN    = "mocked-token";

    // -------------------------------------------------------------------------
    // Mocks and class under test
    // -------------------------------------------------------------------------

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    // -------------------------------------------------------------------------
    // Tests — happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("signin: should return SigninResponseDto for USER role")
    void shouldReturnSigninResponseDtoForUserRole() {

        SigninRequestDto request = new SigninRequestDto(EMAIL, PASSWORD);
        User user = new User(ID, EMAIL, PASSWORD, Role.USER);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        SigninResponseDto result = authService.signin(request);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(TOKEN);
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.role()).isEqualTo(Role.USER);

        verify(userRepository).findByEmail(EMAIL);
        verify(jwtService).generateToken(user);
    }

    @Test
    @DisplayName("signin: should return SigninResponseDto for AGENT role")
    void shouldReturnSigninResponseDtoForAgentRole() {

        SigninRequestDto request = new SigninRequestDto(EMAIL, PASSWORD);
        User user = new User(ID, EMAIL, PASSWORD, Role.AGENT);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        SigninResponseDto result = authService.signin(request);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo(TOKEN);
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.role()).isEqualTo(Role.AGENT);

        verify(userRepository).findByEmail(EMAIL);
        verify(jwtService).generateToken(user);
    }

    // -------------------------------------------------------------------------
    // Tests — error scenarios (edge cases)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("signin: should throw InvalidCredentialsException when email is not found")
    void shouldThrowInvalidCredentialsExceptionWhenEmailNotFound() {

        SigninRequestDto request = new SigninRequestDto(EMAIL, PASSWORD);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email et/ou mot de passe incorrect");

        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("signin: should throw InvalidCredentialsException when password does not match")
    void shouldThrowInvalidCredentialsExceptionWhenPasswordDoesNotMatch() {

        SigninRequestDto request = new SigninRequestDto(EMAIL, "wrong-password");
        User user = new User(ID, EMAIL, PASSWORD, Role.USER);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.signin(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Email et/ou mot de passe incorrect");

        verify(userRepository).findByEmail(EMAIL);
    }

}
