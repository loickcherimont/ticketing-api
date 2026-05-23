package com.github.loickcherimont.ticketing_api.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.micrometer.common.lang.NonNull;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * To enable CORS for RESTful Web API
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "PATCH")
                .allowedOrigins(allowedOrigins)
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * To show informations in Swagger about the API
     * @return {@code OpenAPI} object containing title, version and description about the API
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticketing API")
                        .version("1.0")
                        .description("API de gestion de tickets de support helpdesk"));
    }
}
