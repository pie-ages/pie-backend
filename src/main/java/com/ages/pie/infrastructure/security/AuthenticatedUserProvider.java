package com.ages.pie.infrastructure.security;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Identifica o usuário autenticado a partir do JWT do Supabase já validado
 * pelo Spring Security. O {@code sub} do token é a identidade no Supabase,
 * associada ao customer local pela coluna {@code supabase_user_id}.
 */
@Component
public class AuthenticatedUserProvider {

    private final UserRepository userRepository;

    public AuthenticatedUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID id() {
        UUID supabaseUserId = subject().orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
        return userRepository.findBySupabaseUserId(supabaseUserId)
            .map(User::getId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuário autenticado sem cadastro: " + supabaseUserId));
    }

    public Optional<UUID> optionalId() {
        return subject()
            .flatMap(userRepository::findBySupabaseUserId)
            .map(User::getId);
    }

    private Optional<UUID> subject() {
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
