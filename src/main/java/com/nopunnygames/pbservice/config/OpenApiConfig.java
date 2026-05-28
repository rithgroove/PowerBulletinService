package com.nopunnygames.pbservice.config;

import com.nopunnygames.tanuki.core.config.TanukiOpenApiConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for pb-service.
 */
@Configuration
public class OpenApiConfig {
    /**
     * Creates the OpenAPI configuration.
     */
    public OpenApiConfig() {
    }

    /**
     * Creates pb-service OpenAPI metadata.
     *
     * @return OpenAPI document metadata
     */
    @Bean
    public OpenAPI pbServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Power Bulletin Service API")
                        .version("0.0.1")
                        .description("Tanuki-based canonical data API for Power Bulletin cards, decks, print sets, and effect metadata."))
                .components(new Components().addSecuritySchemes(
                        TanukiOpenApiConfiguration.BEARER_AUTH_SCHEME,
                        TanukiOpenApiConfiguration.bearerScheme()
                ));
    }

    /**
     * Groups pb-service endpoints in Swagger UI.
     *
     * @return grouped OpenAPI definition
     */
    @Bean
    public GroupedOpenApi pbServiceApiGroup() {
        return GroupedOpenApi.builder()
                .group("pb-service")
                .pathsToMatch(
                        "/cards/**",
                        "/card-versions/**",
                        "/card-print-sets/**",
                        "/effects/**",
                        "/decks/**",
                        "/deck-versions/**",
                        "/deck-entries/**",
                        "/exports/**"
                )
                .build();
    }
}
