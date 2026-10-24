package com.ages.pie.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

class AuthenticatedUserProviderTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CompanyRepository companyRepository = mock(CompanyRepository.class);
    private final AuthenticatedUserProvider provider = new AuthenticatedUserProvider(userRepository, companyRepository);

    private UUID registerCustomer(UUID supabaseUserId) {
        UUID localId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(localId);
        when(userRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.of(user));
        return localId;
    }

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
    void id_shouldReturnLocalCustomerId_whenJwtIsAuthenticated() {
        UUID supabaseUserId = UUID.randomUUID();
        UUID localId = registerCustomer(supabaseUserId);
        authenticateWithSubject(supabaseUserId.toString());

        assertThat(provider.id()).isEqualTo(localId);
        assertThat(provider.optionalId()).contains(localId);
    }

    @Test
    void id_shouldThrowNotFound_whenSubjectHasNoCustomer() {
        UUID supabaseUserId = UUID.randomUUID();
        when(userRepository.findBySupabaseUserId(any())).thenReturn(Optional.empty());
        when(companyRepository.findBySupabaseUserId(any())).thenReturn(Optional.empty());
        authenticateWithSubject(supabaseUserId.toString());

        assertThat(catchThrowable(provider::id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(supabaseUserId.toString());
        assertThat(provider.optionalId()).isEmpty();
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
