package com.ages.pie.infrastructure.auth;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.exception.AuthProviderUnavailableException;
import com.ages.pie.application.exception.EmailNotConfirmedException;
import com.ages.pie.application.exception.InvalidCredentialsException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente da API do Supabase Auth (GoTrue). O backend atua como proxy para
 * login, renovação e logout; o Supabase emite e revoga as sessões.
 */
@Component
public class SupabaseAuthClient {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthClient.class);

    private static final Set<String> INVALID_CREDENTIAL_CODES = Set.of(
        "invalid_credentials",
        "invalid_grant",
        "refresh_token_not_found",
        "refresh_token_already_used",
        "session_not_found",
        "bad_jwt"
    );

    private final RestClient restClient;
    private final SupabaseAuthProperties props;

    @Autowired
    public SupabaseAuthClient(SupabaseAuthProperties props) {
        this(props, RestClient.builder());
    }

    SupabaseAuthClient(SupabaseAuthProperties props, RestClient.Builder builder) {
        this.props = props;
        this.restClient = builder
            .baseUrl(props.getUrl() + "/auth/v1")
            .defaultHeader("apikey", props.getPublishableKey())
            .build();
    }

    public Session passwordGrant(String email, String password) {
        return token("password", Map.of("email", email, "password", password));
    }

    public Session refresh(String refreshToken) {
        return token("refresh_token", Map.of("refresh_token", refreshToken));
    }

    /** Encerra a sessão do token informado e revoga seus refresh tokens. */
    public void logout(String accessToken) {
        execute(() -> restClient.post()
            .uri("/logout?scope=local")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .toBodilessEntity());
    }

    private Session token(String grantType, Map<String, String> body) {
        return execute(() -> restClient.post()
            .uri(uri -> uri.path("/token").queryParam("grant_type", grantType).build())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(Session.class));
    }

    private <T> T execute(java.util.function.Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientResponseException e) {
            throw translate(e);
        } catch (ResourceAccessException e) {
            logger.error("Supabase Auth inacessível", e);
            throw new AuthProviderUnavailableException("Serviço de autenticação indisponível");
        }
    }

    private RuntimeException translate(RestClientResponseException e) {
        if (e.getStatusCode().is5xxServerError()) {
            logger.error("Supabase Auth respondeu {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return new AuthProviderUnavailableException("Serviço de autenticação indisponível");
        }
        String code = errorCode(e);
        if ("email_not_confirmed".equals(code)) {
            return new EmailNotConfirmedException("E-mail ainda não confirmado");
        }
        if (INVALID_CREDENTIAL_CODES.contains(code) || e.getStatusCode().value() == 401
                || e.getStatusCode().value() == 403) {
            return new InvalidCredentialsException("Credenciais inválidas");
        }
        logger.error("Supabase Auth respondeu {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        return new AuthProviderUnavailableException("Falha ao comunicar com o serviço de autenticação");
    }

    private static String errorCode(RestClientResponseException e) {
        try {
            GoTrueError error = e.getResponseBodyAs(GoTrueError.class);
            if (error == null) {
                return null;
            }
            return error.errorCode() != null ? error.errorCode() : error.error();
        } catch (RuntimeException parseFailure) {
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Session(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("user") SessionUser user
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SessionUser(UUID id, String email) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoTrueError(@JsonProperty("error_code") String errorCode, String error) {
    }
}
