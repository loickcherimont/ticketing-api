package com.github.loickcherimont.ticketing_api.dto;

import java.util.Map;

public record ApiError(
    int status,
    String error,
    String message,
    Map<String, String> fieldErrors
) {}
