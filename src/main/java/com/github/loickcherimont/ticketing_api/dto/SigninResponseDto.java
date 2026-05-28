package com.github.loickcherimont.ticketing_api.dto;

import com.github.loickcherimont.ticketing_api.models.Role;

public record SigninResponseDto(
    String token,
    String email,
    Role role
) {}
