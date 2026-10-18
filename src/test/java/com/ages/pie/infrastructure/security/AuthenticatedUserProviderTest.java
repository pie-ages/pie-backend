package com.ages.pie.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

class AuthenticatedUserProviderTest {

    private final AuthenticatedUserProvider provider = new AuthenticatedUserProvider();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticateWithSubject(String subject) {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "ES256")
            .subject(subject)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private static int statusOf(Throwable thrown) {
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        return ((ResponseStatusException) thrown).getStatusCode().value();
    }

    @Test
    void id_shouldReturnSubject_whenJwtIsAuthenticated() {
        UUID userId = UUID.randomUUID();
        authenticateWithSubject(userId.toString());

        assertThat(provider.id()).isEqualTo(userId);
        assertThat(provider.optionalId()).contains(userId);
    }

    @Test
    void id_shouldThrowUnauthorized_whenThereIsNoAuthentication() {
        assertThat(statusOf(catchThrowable(provider::id))).isEqualTo(401);
        assertThat(provider.optionalId()).isEmpty();
    }

    @Test
    void id_shouldThrowUnauthorized_whenAuthenticationIsNotJwt() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("user", "pass"));

        assertThat(statusOf(catchThrowable(provider::id))).isEqualTo(401);
    }

    @Test
    void id_shouldThrowUnauthorized_whenSubjectIsNotUuid() {
        authenticateWithSubject("not-a-uuid");

        assertThat(statusOf(catchThrowable(provider::id))).isEqualTo(401);
    }
}
