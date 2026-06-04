package com.github.loickcherimont.ticketing_api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Allow client to use JWT to access secured routes on Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer");
    }

    /**
     * To show informations in Swagger about the API and implement JWT into Swagger UI.
     * 
     * @return {@code OpenAPI} object containing title, version and description
     *         about the API
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info()
                        .title("Ticketing API")
                        .description("Helpdesk management API for tickets")
                        .version("1.0")
                        .contact(new Contact().name("Loick CHERIMONT").email("loickcherimont@gmail.com")
                                .url("https://github.com/loickcherimont"))
                        .license(new License().name("MIT License")
                                .url("https://github.com/loickcherimont/ticketing-api/blob/main/LICENSE")));
    }
}
