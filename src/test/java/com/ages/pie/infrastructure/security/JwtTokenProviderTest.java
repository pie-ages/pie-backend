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
    void gerarTokenProduzJwtComTresPartes() {
        String token = provider.gerarToken(UUID.randomUUID());

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void validarTokenAceitaTokenGeradoPeloProvider() {
        String token = provider.gerarToken(UUID.randomUUID());

        assertThat(provider.validarToken(token)).isTrue();
    }

    @Test
    void extrairUserIdFazRoundTripDoUuid() {
        UUID userId = UUID.randomUUID();

        String token = provider.gerarToken(userId);

        assertThat(provider.extrairUserId(token)).isEqualTo(userId);
    }

    @Test
    void validarTokenRejeitaTokenMalformado() {
        assertThat(provider.validarToken("")).isFalse();
        assertThat(provider.validarToken("nao-e-um-jwt")).isFalse();
        assertThat(provider.validarToken("a.b.c")).isFalse();
    }

    @Test
    void validarTokenRejeitaAssinaturaDeOutroSegredo() {
        JwtTokenProvider outro = new JwtTokenProvider(
            "outro-segredo-completamente-diferente-do-primeiro", Duration.ofHours(1));
        String token = outro.gerarToken(UUID.randomUUID());

        assertThat(provider.validarToken(token)).isFalse();
    }

    @Test
    void validarTokenRejeitaTokenExpirado() {
        JwtTokenProvider expirado = new JwtTokenProvider(SECRET, Duration.ofSeconds(-1));
        String token = expirado.gerarToken(UUID.randomUUID());

        assertThat(provider.validarToken(token)).isFalse();
    }

    @Test
    void extrairUserIdLancaParaTokenComAssinaturaInvalida() {
        JwtTokenProvider outro = new JwtTokenProvider(
            "outro-segredo-completamente-diferente-do-primeiro", Duration.ofHours(1));
        String token = outro.gerarToken(UUID.randomUUID());

        assertThatThrownBy(() -> provider.extrairUserId(token)).isInstanceOf(JwtException.class);
    }
}
