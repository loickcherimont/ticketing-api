package com.github.loickcherimont.ticketing_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data to create a new ticket")
public record TicketRequestDto(

    @NotBlank(message = "Le titre ne peut être vide. Veuillez renseigner un titre s'il vous plaît.")
    @Size(max = 100, message = "Le titre ne peut dépasser 100 caractères. Veuillez le raccourcir s'il vous plaît.")
    String title,

    @NotBlank(message = "La description ne peut être vide. Veuillez renseigner une description s'il vous plaît.")
    String description
) {}
