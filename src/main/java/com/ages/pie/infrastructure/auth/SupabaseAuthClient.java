package com.ages.pie.infrastructure.auth;

import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Component
public class SupabaseAuthClient {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthClient.class);
    private static final String EMAIL_EXISTS = "email_exists";
    private static final String WEAK_PASSWORD = "weak_password";
    private static final String ALREADY_REGISTERED = "already registered";

    private final RestClient restClient;
    private final SupabaseAuthProperties props;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Autowired
    public SupabaseAuthClient(SupabaseAuthProperties props) {
        this(props, createRestClient());
    }

    SupabaseAuthClient(SupabaseAuthProperties props, RestClient restClient) {
        this.props = props;
        this.restClient = restClient;
    }

    public UUID createUser(String email, String password) {
        requireConfigured();

        String response;
        try {
            response = restClient.post()
                    .uri(adminUsersUrl())
                    .headers(this::authHeaders)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", email, "password", password, "email_confirm", true))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw translateCreateError(e, email);
        } catch (RestClientException e) {
            logger.error("Falha de comunicação com o Supabase Auth ao criar usuário", e);
            throw badGateway();
        }

        return extractId(response);
    }

    public void deleteUser(UUID supabaseUserId) {
        requireConfigured();

        try {
            restClient.delete()
                    .uri(adminUsersUrl() + "/" + supabaseUserId)
                    .headers(this::authHeaders)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                logger.warn("Usuário {} não existe no Supabase Auth, nada a remover", supabaseUserId);
                return;
            }
            logger.error("Supabase Auth respondeu {} ao remover o usuário {}: {}",
                    e.getStatusCode(), supabaseUserId, e.getResponseBodyAsString());
            throw badGateway();
        } catch (RestClientException e) {
            logger.error("Falha de comunicação com o Supabase Auth ao remover o usuário {}", supabaseUserId, e);
            throw badGateway();
        }
    }

    private RuntimeException translateCreateError(RestClientResponseException e, String email) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();

        if (status == HttpStatus.BAD_REQUEST.value() || status == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
            String errorCode = readField(body, "error_code");
            if (isEmailAlreadyRegistered(errorCode, body)) {
                return new BusinessException("Email já cadastrado: " + email);
            }
            logger.warn("Supabase Auth recusou o cadastro ({}): {}", e.getStatusCode(), body);
            if (WEAK_PASSWORD.equals(errorCode)) {
                return new IllegalArgumentException("A senha não atende aos requisitos mínimos de segurança");
            }
            return new IllegalArgumentException("Dados inválidos para o cadastro");
        }

        logger.error("Supabase Auth respondeu {} ao criar usuário: {}", e.getStatusCode(), body);
        return badGateway();
    }

    private boolean isEmailAlreadyRegistered(String errorCode, String body) {
        return EMAIL_EXISTS.equals(errorCode) || body.toLowerCase().contains(ALREADY_REGISTERED);
    }

    private UUID extractId(String response) {
        if (response == null) {
            logger.error("Supabase Auth devolveu corpo vazio ao criar usuário");
            throw badGateway();
        }
        try {
            return UUID.fromString(mapper.readTree(response).path("id").asString(""));
        } catch (JacksonException | IllegalArgumentException e) {
            logger.error("Resposta inesperada do Supabase Auth ao criar usuário: {}", response);
            throw badGateway();
        }
    }

    private String readField(String body, String field) {
        try {
            return mapper.readTree(body).path(field).asString("");
        } catch (JacksonException e) {
            return "";
        }
    }

    private void requireConfigured() {
        if (props.getUrl().isBlank() || props.getServiceRoleKey().isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "O cadastro está indisponível no momento. Tente novamente mais tarde.");
        }
    }

    private void authHeaders(HttpHeaders headers) {
        headers.set("Authorization", "Bearer " + props.getServiceRoleKey());
        headers.set("apikey", props.getServiceRoleKey());
    }

    private String adminUsersUrl() {
        return props.getUrl() + "/auth/v1/admin/users";
    }

    private static ResponseStatusException badGateway() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Não foi possível concluir o cadastro. Tente novamente.");
    }

    private static RestClient createRestClient() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(factory).build();
    }
}
