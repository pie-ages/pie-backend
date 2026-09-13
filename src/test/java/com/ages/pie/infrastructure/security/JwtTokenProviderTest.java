package com.ages.pie.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.UUID;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = "pie-test-secret-please-keep-this-at-least-32-bytes";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, Duration.ofHours(1));

    @Test
    void tokenGeradoTemTresPartes() {
        String token = provider.generateToken(UUID.randomUUID());

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void tokenValidoEAceito() {
        String token = provider.generateToken(UUID.randomUUID());

        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    void userIdERecuperadoDoToken() {
        UUID userId = UUID.randomUUID();

        String token = provider.generateToken(userId);

        assertThat(provider.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void tokenMalformadoERejeitado() {
        assertThat(provider.validateToken("")).isFalse();
        assertThat(provider.validateToken("nao-e-um-jwt")).isFalse();
        assertThat(provider.validateToken("a.b.c")).isFalse();
    }

    @Test
    void tokenComAssinaturaDeOutroSegredoERejeitado() {
        JwtTokenProvider outro = new JwtTokenProvider(
            "outro-segredo-completamente-diferente-do-primeiro", Duration.ofHours(1));
        String token = outro.generateToken(UUID.randomUUID());

        assertThat(provider.validateToken(token)).isFalse();
    }

    @Test
    void tokenExpiradoERejeitado() {
        JwtTokenProvider expirado = new JwtTokenProvider(SECRET, Duration.ofSeconds(-1));
        String token = expirado.generateToken(UUID.randomUUID());

        assertThat(provider.validateToken(token)).isFalse();
    }

    @Test
    void extrairUserIdDeTokenComAssinaturaInvalidaLancaExcecao() {
        JwtTokenProvider outro = new JwtTokenProvider(
            "outro-segredo-completamente-diferente-do-primeiro", Duration.ofHours(1));
        String token = outro.generateToken(UUID.randomUUID());

        assertThatThrownBy(() -> provider.extractUserId(token)).isInstanceOf(JwtException.class);
    }
}
