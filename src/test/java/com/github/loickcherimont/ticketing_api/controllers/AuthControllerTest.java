package com.github.loickcherimont.ticketing_api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loickcherimont.ticketing_api.dto.SigninRequestDto;
import com.github.loickcherimont.ticketing_api.dto.SigninResponseDto;
import com.github.loickcherimont.ticketing_api.models.Role;
import com.github.loickcherimont.ticketing_api.services.AuthService;
import com.github.loickcherimont.ticketing_api.services.JwtService;

/**
 * Web MVC tests for {@link AuthController}.
 *
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // don't execute Filter beans from context during test (remove security)
public class AuthControllerTest {

        private static final String BASE_URI_PATH = "/api/auth/signin";
        private static final String EMAIL = "user@gmail.com";
        private static final String PASSWORD = "secret-password";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsService userDetailsService;

        // -------------------------------------------------------------------------
        // Tests — happy paths
        // -------------------------------------------------------------------------

        @Test
        @DisplayName("signin: should return HTTP 200 OK for valid request (email, password)")
        void shouldReturnHttp200WithSigninResponseDto() throws Exception {

                SigninRequestDto signinRequestDto = new SigninRequestDto(EMAIL, PASSWORD);

                SigninResponseDto signinResponseDto = new SigninResponseDto("mocked-token", EMAIL, Role.USER);

                when(authService.signin(any(SigninRequestDto.class))).thenReturn(signinResponseDto);

                this.mockMvc.perform(
                                post(BASE_URI_PATH)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(signinRequestDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("mocked-token"))
                                .andExpect(jsonPath("$.email").value(EMAIL))
                                .andExpect(jsonPath("$.role").value(Role.USER.name()));

                verify(authService).signin(any(SigninRequestDto.class));
        }

        // -------------------------------------------------------------------------
        // Tests — error scenarios (edge cases)
        // -------------------------------------------------------------------------

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { " ", "\n", "\t", "\r" })
        @DisplayName("signin: should return HTTP 400 Bad Request when email is blank")
        void shouldReturnHttp400WhenEmailIsBlank(String email) throws Exception {
                SigninRequestDto signinRequestDto = new SigninRequestDto(email, "secret-password");

                this.mockMvc.perform(
                                post(BASE_URI_PATH)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(signinRequestDto)))
                                .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { " ", "\n", "\t", "\r" })
        @DisplayName("signin: should return HTTP 400 Bad Request when password is blank")
        void shouldReturnHttp400WhenPasswordIsBlank(String password) throws Exception {
                SigninRequestDto signinRequestDto = new SigninRequestDto("user@gmail.com", password);

                this.mockMvc.perform(
                                post(BASE_URI_PATH)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(signinRequestDto)))
                                .andExpect(status().isBadRequest());
        }

        @ParameterizedTest(name = "[{index}] email=''{0}'' password=''{1}'' should return HTTP 400 Bad Request")
        @MethodSource("blankInputs")
        @DisplayName("signin: should return HTTP 400 Bad Request when email and password are blank")
        void shouldReturnHttp400WhenAllSigninFieldsAreBlank(String email, String password) throws Exception {
                SigninRequestDto signinRequestDto = new SigninRequestDto(email, password);

                this.mockMvc.perform(
                                post(BASE_URI_PATH)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(signinRequestDto)))
                                .andExpect(status().isBadRequest());
        }

        /**
         * 
         * Method containing in Stream all blank cases.
         * 
         * @return Stream of null, "", "valide", " ".
         */
        static Stream<Arguments> blankInputs() {

                return Stream.of(
                                Arguments.of(null, null),
                                Arguments.of(null, ""),
                                Arguments.of(null, " "),
                                Arguments.of(null, "secret-password"),
                                Arguments.of("", null),
                                Arguments.of("", ""),
                                Arguments.of("", " "),
                                Arguments.of("", "secret-password"),
                                Arguments.of(" ", null),
                                Arguments.of(" ", ""),
                                Arguments.of(" ", " "),
                                Arguments.of(" ", "secret-password"),
                                Arguments.of("user@gmail.com", null),
                                Arguments.of("user@gmail.com", ""),
                                Arguments.of("user@gmail.com", " "));

        }

}
