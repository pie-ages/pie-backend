package com.ages.pie.infrastructure.security;

import java.util.UUID;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticatedUserProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final HttpServletRequest request;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticatedUserProvider(HttpServletRequest request, JwtTokenProvider jwtTokenProvider) {
        this.request = request;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public UUID id() {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null
                || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw unauthenticated(null);
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw unauthenticated(null);
        }

        try {
            return jwtTokenProvider.extractUserId(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw unauthenticated(e);
        }
    }

    private ResponseStatusException unauthenticated(Throwable cause) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado", cause);
    }
}
