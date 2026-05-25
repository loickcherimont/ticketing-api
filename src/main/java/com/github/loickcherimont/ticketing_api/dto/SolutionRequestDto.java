package com.github.loickcherimont.ticketing_api.dto;

import jakarta.validation.constraints.NotBlank;

public record SolutionRequestDto(
    @NotBlank(message = "La solution ne peut être vide. Veuillez renseigner une solution s'il vous plaît.")
    String solution
) {}
