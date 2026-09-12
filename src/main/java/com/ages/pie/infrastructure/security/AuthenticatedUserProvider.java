package com.ages.pie.infrastructure.security;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Identificação provisória do usuário autenticado: enquanto não existe
 * validação de token, o id vem do header {@code X-User-Id}. Quando a
 * autenticação real entrar, apenas o corpo de {@link #id()} muda.
 */
@Component
public class AuthenticatedUserProvider {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final HttpServletRequest request;

    public AuthenticatedUserProvider(HttpServletRequest request) {
        this.request = request;
    }

    public UUID id() {
        String header = request.getHeader(USER_ID_HEADER);
        if (header == null || header.isBlank()) {
            throw unauthenticated(null);
        }

        try {
            return UUID.fromString(header.trim());
        } catch (IllegalArgumentException e) {
            throw unauthenticated(e);
        }
    }

    private ResponseStatusException unauthenticated(Throwable cause) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado", cause);
    }
}
