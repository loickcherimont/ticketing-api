package com.github.loickcherimont.ticketing_api.dto;

import jakarta.validation.constraints.NotBlank;

public record SigninRequestDto(

        @NotBlank(message = "L'email ne peut être vide. Veuillez le renseigner s'il vous plaît.") String email,

        @NotBlank(message = "Le mot de passe ne peut être vide. Veuillez le renseigner s'il vous plaît.") String password) {
}
