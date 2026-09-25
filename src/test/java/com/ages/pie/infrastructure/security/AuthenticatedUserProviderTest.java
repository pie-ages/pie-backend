package com.ages.pie.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class AuthenticatedUserProviderTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hmac-sha256";

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, Duration.ofHours(1));

    private static int statusOf(Throwable thrown) {
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        return ((ResponseStatusException) thrown).getStatusCode().value();
    }

    private AuthenticatedUserProvider providerWithAuthorization(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (value != null) {
            request.addHeader("Authorization", value);
        }
        return new AuthenticatedUserProvider(request, jwtTokenProvider);
    }

    @Test
    void id_shouldReturnUserId_whenBearerTokenIsValid() {
        UUID userId = UUID.randomUUID();

        assertThat(providerWithAuthorization("Bearer " + jwtTokenProvider.generateToken(userId)).id())
            .isEqualTo(userId);
    }

    @Test
    void id_shouldThrowUnauthorized_whenAuthorizationIsMissing() {
        Throwable thrown = catchThrowable(() -> providerWithAuthorization(null).id());

        assertThat(statusOf(thrown)).isEqualTo(401);
    }

    @Test
    void id_shouldThrowUnauthorized_whenAuthorizationIsNotBearer() {
        Throwable thrown = catchThrowable(() -> providerWithAuthorization("Basic credentials").id());

        assertThat(statusOf(thrown)).isEqualTo(401);
    }

    @Test
    void id_shouldThrowUnauthorized_whenBearerTokenIsInvalid() {
        Throwable thrown = catchThrowable(() -> providerWithAuthorization("Bearer invalid-token").id());

        assertThat(statusOf(thrown)).isEqualTo(401);
    }

    @Test
    void id_shouldThrowUnauthorized_whenBearerTokenIsExpired() {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(SECRET, Duration.ofSeconds(-1));
        String token = expiredTokenProvider.generateToken(UUID.randomUUID());

        Throwable thrown = catchThrowable(() -> providerWithAuthorization("Bearer " + token).id());

        assertThat(statusOf(thrown)).isEqualTo(401);
    }

    @Test
    void id_shouldThrowUnauthorized_whenBearerTokenHasAnotherSignature() {
        JwtTokenProvider anotherTokenProvider = new JwtTokenProvider(
            "another-test-secret-long-enough-for-hmac-sha256", Duration.ofHours(1));
        String token = anotherTokenProvider.generateToken(UUID.randomUUID());

        Throwable thrown = catchThrowable(() -> providerWithAuthorization("Bearer " + token).id());

        assertThat(statusOf(thrown)).isEqualTo(401);
    }
}
