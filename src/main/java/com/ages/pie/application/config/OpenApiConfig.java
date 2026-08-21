package com.ages.pie.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Configuração técnica do Swagger/OpenAPI. Não deve conter nenhuma lógica
 * de negócio.
 *
 * Documentar a API bem aqui compensa duplamente: além de facilitar o
 * desenvolvimento do time, o contrato OpenAPI gerado pode alimentar
 * ferramentas como openapi-typescript no front em React Native/TS, para
 * gerar os types automaticamente em vez de escrever na mão.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .addSecurityItem(
                new SecurityRequirement().addList(securitySchemeName)
            )
            .components(
                new Components().addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
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