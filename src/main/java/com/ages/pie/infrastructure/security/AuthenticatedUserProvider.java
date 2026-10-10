package com.ages.pie.infrastructure.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Identifica o usuário autenticado a partir do JWT do Supabase já validado
 * pelo Spring Security. O {@code sub} do token é o id do customer.
 */
@Component
public class AuthenticatedUserProvider {

    public UUID id() {
        return optionalId().orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
    }

    public Optional<UUID> optionalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(jwtAuthentication.getToken().getSubject()));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
