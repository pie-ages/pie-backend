package com.ages.pie.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "X-User-Id";

        return new OpenAPI()
            .components(
                new Components().addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name("X-User-Id")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("UUID do usuário autenticado (company ou customer)")
                )
            )
            .info(
                new Info()
                    .title("API Piê Consultoria de Imagem")
                    .description("Documentação da API do sistema Piê Consultoria de Imagem")
                    .version("1.0.0")
            );
    }
}