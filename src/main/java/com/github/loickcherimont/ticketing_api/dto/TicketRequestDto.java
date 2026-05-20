package com.github.loickcherimont.ticketing_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Data to create a new ticket")
public record TicketRequestDto(
    String title,
    String description
) {}
