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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDto> signin(@RequestBody SigninRequestDto signinRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.signin(signinRequestDto));
    }
}
