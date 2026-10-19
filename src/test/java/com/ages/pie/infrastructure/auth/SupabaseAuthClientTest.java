package com.ages.pie.infrastructure.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.UUID;

import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.exception.AuthProviderUnavailableException;
import com.ages.pie.application.exception.EmailNotConfirmedException;
import com.ages.pie.application.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SupabaseAuthClientTest {

    private static final String BASE = "https://proj.supabase.co/auth/v1";

    private MockRestServiceServer server;
    private SupabaseAuthClient client;

    @BeforeEach
    void setUp() {
        SupabaseAuthProperties props = new SupabaseAuthProperties();
        props.setUrl("https://proj.supabase.co");
        props.setPublishableKey("pub-key");
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SupabaseAuthClient(props, builder);
    }

    private static String sessionJson(UUID userId) {
        return """
            {"access_token":"acc","token_type":"bearer","expires_in":3600,"expires_at":1,
             "refresh_token":"ref","user":{"id":"%s","email":"ana@email.com","role":"authenticated"}}
            """.formatted(userId);
    }

    private static String errorJson(String code) {
        return "{\"code\":400,\"error_code\":\"" + code + "\",\"msg\":\"x\"}";
    }

    @Test
    void passwordGrantEnviaApikeyECredenciaisERetornaSessao() {
        UUID userId = UUID.randomUUID();
        server.expect(requestTo(BASE + "/token?grant_type=password"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("apikey", "pub-key"))
            .andExpect(content().json("{\"email\":\"ana@email.com\",\"password\":\"senha\"}"))
            .andRespond(withSuccess(sessionJson(userId), MediaType.APPLICATION_JSON));

        SupabaseAuthClient.Session session = client.passwordGrant("ana@email.com", "senha");

        assertThat(session.accessToken()).isEqualTo("acc");
        assertThat(session.refreshToken()).isEqualTo("ref");
        assertThat(session.expiresIn()).isEqualTo(3600);
        assertThat(session.user().id()).isEqualTo(userId);
        server.verify();
    }

    @Test
    void passwordGrantComCredenciaisInvalidasLancaInvalidCredentials() {
        server.expect(requestTo(BASE + "/token?grant_type=password"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body(errorJson("invalid_credentials")));

        assertThatThrownBy(() -> client.passwordGrant("ana@email.com", "errada"))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void passwordGrantComEmailNaoConfirmadoLancaEmailNotConfirmed() {
        server.expect(requestTo(BASE + "/token?grant_type=password"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body(errorJson("email_not_confirmed")));

        assertThatThrownBy(() -> client.passwordGrant("ana@email.com", "senha"))
            .isInstanceOf(EmailNotConfirmedException.class);
    }

    @Test
    void erro5xxLancaAuthProviderUnavailable() {
        server.expect(requestTo(BASE + "/token?grant_type=password")).andRespond(withServerError());

        assertThatThrownBy(() -> client.passwordGrant("ana@email.com", "senha"))
            .isInstanceOf(AuthProviderUnavailableException.class);
    }

    @Test
    void refreshEnviaRefreshTokenERetornaNovaSessao() {
        server.expect(requestTo(BASE + "/token?grant_type=refresh_token"))
            .andExpect(header("apikey", "pub-key"))
            .andExpect(content().json("{\"refresh_token\":\"ref-antigo\"}"))
            .andRespond(withSuccess(sessionJson(UUID.randomUUID()), MediaType.APPLICATION_JSON));

        assertThat(client.refresh("ref-antigo").accessToken()).isEqualTo("acc");
        server.verify();
    }

    @Test
    void refreshComTokenJaUsadoLancaInvalidCredentials() {
        server.expect(requestTo(BASE + "/token?grant_type=refresh_token"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body(errorJson("refresh_token_already_used")));

        assertThatThrownBy(() -> client.refresh("ref"))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void logoutEnviaBearerDoUsuario() {
        server.expect(requestTo(BASE + "/logout?scope=local"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("apikey", "pub-key"))
            .andExpect(header("Authorization", "Bearer acc"))
            .andRespond(withNoContent());

        client.logout("acc");
        server.verify();
    }
}
