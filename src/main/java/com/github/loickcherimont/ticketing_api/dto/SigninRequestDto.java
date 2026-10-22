package com.github.loickcherimont.ticketing_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SigninRequestDto(

        @NotBlank(message = "L'email ne peut être vide. Veuillez le renseigner s'il vous plaît.")
        @Email(message = "L'email n'est pas valide. Veuillez renseigner une adresse email correcte s'il vous plaît.")
        @Size(max = 100, message = "L'email ne peut dépasser 100 caractères. Veuillez le raccourcir s'il vous plaît.")
        String email,

        @NotBlank(message = "Le mot de passe ne peut être vide. Veuillez le renseigner s'il vous plaît.") String password) {
}
