package com.github.loickcherimont.ticketing_api.services.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.loickcherimont.ticketing_api.dto.SigninRequestDto;
import com.github.loickcherimont.ticketing_api.dto.SigninResponseDto;
import com.github.loickcherimont.ticketing_api.exceptions.InvalidCredentialsException;
import com.github.loickcherimont.ticketing_api.models.User;
import com.github.loickcherimont.ticketing_api.repository.UserRepository;
import com.github.loickcherimont.ticketing_api.services.AuthService;
import com.github.loickcherimont.ticketing_api.services.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SigninResponseDto signin(SigninRequestDto signinRequestDto) {

        User user = userRepository.findByEmail(signinRequestDto.email()).orElse(null);

        if (user == null || !passwordEncoder.matches(signinRequestDto.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Email et/ou mot de passe incorrect");
        }

        String token = jwtService.generateToken(user);

        return new SigninResponseDto(
            token,
            user.getUsername(),
            user.getRole()
        );
    }

}
