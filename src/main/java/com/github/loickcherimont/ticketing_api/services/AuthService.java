package com.github.loickcherimont.ticketing_api.services;

import com.github.loickcherimont.ticketing_api.dto.SigninRequestDto;
import com.github.loickcherimont.ticketing_api.dto.SigninResponseDto;

public interface AuthService {
    SigninResponseDto signin(SigninRequestDto signinRequestDto);
}
