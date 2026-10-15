package com.github.loickcherimont.ticketing_api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loickcherimont.ticketing_api.dto.SigninRequestDto;
import com.github.loickcherimont.ticketing_api.dto.SigninResponseDto;
import com.github.loickcherimont.ticketing_api.models.Role;
import com.github.loickcherimont.ticketing_api.models.User;
import com.github.loickcherimont.ticketing_api.repository.UserRepository;
import com.github.loickcherimont.ticketing_api.services.impl.JwtServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integration")
class SecurityIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtServiceImpl jwtService;

    @Test
    @DisplayName("POST /api/auth/signin: should return HTTP 200 with a real JWT for valid credentials")
    void signin_withValidCredentials_returnsRealJwt() throws Exception {
        userRepository.save(new User(null, "alice@banque.fr",
                passwordEncoder.encode("MotDePasse123"), Role.USER));

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SigninRequestDto("alice@banque.fr", "MotDePasse123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("alice@banque.fr"))
                .andReturn();

        SigninResponseDto resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), SigninResponseDto.class);
        assertThat(jwtService.extractUsername(resp.token())).isEqualTo("alice@banque.fr");

        mockMvc.perform(get("/api/tickets").header("Authorization", "Bearer " + resp.token())).andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // Failure cases — the full chain (DB + JWT + filter) must reject them
    // -------------------------------------------------------------------------

    /**
     * Wrong password on signin: the real AuthService throws
     * InvalidCredentialsException, GlobalExceptionHandler converts it to
     * a 401 with a French message. Full stack: controller → service →
     * password check → exception handler.
     */
    @Test
    @DisplayName("POST /api/auth/signin: should return HTTP 401 for a wrong password")
    void signin_withWrongPassword_returnsHttp401() throws Exception {
        userRepository.save(new User(null, "bob@banque.fr",
                passwordEncoder.encode("MotDePasse123"), Role.USER));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SigninRequestDto("bob@banque.fr", "MauvaisMotDePasse"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email et/ou mot de passe incorrect"));
    }

    /**
     * Garbage token on a protected endpoint: the real JWT filter tries to
     * parse it, fails, and the request is rejected with 401. This proves
     * the filter + SecurityConfig work together against forged tokens.
     */
    @Test
    @DisplayName("GET /api/tickets: should return HTTP 401 for a forged token")
    void getTickets_withForgedToken_returnsHttp401() throws Exception {

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer not.a.valid.token"))
                .andExpect(status().isUnauthorized());
    }
}