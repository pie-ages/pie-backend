package com.ages.pie.infrastructure.security;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.CompanyRole;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticatedUserProvider {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public AuthenticatedUserProvider(UserRepository userRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    public UUID id() {
        UUID supabaseUserId = subject().orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
        return userRepository.findBySupabaseUserId(supabaseUserId)
            .map(User::getId)
            .or(() -> companyRepository.findBySupabaseUserId(supabaseUserId).map(Company::getId))
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuário autenticado sem cadastro: " + supabaseUserId));
    }

    public Optional<UUID> optionalId() {
        return subject()
            .flatMap(supabaseId -> userRepository.findBySupabaseUserId(supabaseId)
                .map(User::getId)
                .or(() -> companyRepository.findBySupabaseUserId(supabaseId).map(Company::getId)));
    }

    public boolean isAdmin() {
        return subject()
            .flatMap(companyRepository::findBySupabaseUserId)
            .map(c -> c.getRole() == CompanyRole.ADMIN)
            .orElse(false);
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
