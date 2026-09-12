package com.ages.pie.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class AuthenticatedUserProviderTest {

    private static int statusOf(Throwable thrown) {
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        return ((ResponseStatusException) thrown).getStatusCode().value();
    }

    private static AuthenticatedUserProvider providerWithHeader(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (value != null) {
            request.addHeader("X-User-Id", value);
        }
        return new AuthenticatedUserProvider(request);
    }

    @Test
    void id_shouldReturnUserId_whenHeaderIsValid() {
        UUID userId = UUID.randomUUID();

        assertThat(providerWithHeader(userId.toString()).id()).isEqualTo(userId);
    }

    @Test
    void id_shouldThrowUnauthorized_whenHeaderIsMissing() {
        Throwable thrown = catchThrowable(() -> providerWithHeader(null).id());

        assertThat(statusOf(thrown)).isEqualTo(401);
    }

    @Test
    void id_shouldThrowUnauthorized_whenHeaderIsBlank() {
        Throwable thrown = catchThrowable(() -> providerWithHeader("   ").id());

        assertThat(statusOf(thrown)).isEqualTo(401);
    }

    @Test
    void id_shouldThrowUnauthorized_whenHeaderIsNotAUuid() {
        Throwable thrown = catchThrowable(() -> providerWithHeader("nao-e-uuid").id());

        assertThat(statusOf(thrown)).isEqualTo(401);
    }
}
