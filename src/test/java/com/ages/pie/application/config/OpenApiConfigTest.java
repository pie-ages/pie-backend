package com.ages.pie.application.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

    @Test
    void declaresBearerSchemeForProtectedEndpoints() {
        SecurityScheme scheme = new OpenApiConfig().customOpenAPI()
                .getComponents().getSecuritySchemes().get("bearerAuth");

        assertThat(scheme).isNotNull();
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }
}