package com.github.loickcherimont.ticketing_api.dto;

public record SigninRequestDto(
    String email,
    String password
) {}
