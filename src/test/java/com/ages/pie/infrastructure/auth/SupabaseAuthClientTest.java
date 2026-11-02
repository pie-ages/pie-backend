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
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

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

    private void respondError(String path, HttpStatus status, String code) {
        server.expect(requestTo(BASE + path))
            .andRespond(withStatus(status).contentType(MediaType.APPLICATION_JSON).body(errorJson(code)));
    }

    private static void assertResponseStatus(ThrowingCallable call, HttpStatus status, String reason) {
        assertThatThrownBy(call)
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(error -> {
                ResponseStatusException exception = (ResponseStatusException) error;
                assertThat(exception.getStatusCode().value()).isEqualTo(status.value());
                assertThat(exception.getReason()).isEqualTo(reason);
            });
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

    @Test
    void recoverEnviaApikeyEEmail() {
        server.expect(requestTo(BASE + "/recover"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("apikey", "pub-key"))
            .andExpect(content().json("{\"email\":\"ana@email.com\"}"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.recover("ana@email.com");
        server.verify();
    }

    @Test
    void verifyRecoveryEnviaTipoEmailECodigoERetornaSessao() {
        UUID userId = UUID.randomUUID();
        server.expect(requestTo(BASE + "/verify"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("apikey", "pub-key"))
            .andExpect(content().json("{\"type\":\"recovery\",\"email\":\"ana@email.com\",\"token\":\"123456\"}"))
            .andRespond(withSuccess(sessionJson(userId), MediaType.APPLICATION_JSON));

        SupabaseAuthClient.Session session = client.verifyRecovery("ana@email.com", "123456");

        assertThat(session.accessToken()).isEqualTo("acc");
        assertThat(session.refreshToken()).isEqualTo("ref");
        assertThat(session.user().id()).isEqualTo(userId);
        server.verify();
    }

    @Test
    void updatePasswordEnviaBearerDoUsuarioENovaSenha() {
        server.expect(requestTo(BASE + "/user"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(header("apikey", "pub-key"))
            .andExpect(header("Authorization", "Bearer acc"))
            .andExpect(content().json("{\"password\":\"nova-senha\"}"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.updatePassword("acc", "nova-senha");
        server.verify();
    }

    @Test
    void verifyRecoveryComCodigoInvalidoOuExpiradoLanca400() {
        respondError("/verify", HttpStatus.FORBIDDEN, "otp_expired");

        assertResponseStatus(() -> client.verifyRecovery("ana@email.com", "000000"),
            HttpStatus.BAD_REQUEST, "Código inválido ou expirado");
    }

    @Test
    void updatePasswordComSenhaIgualAAtualLanca400() {
        respondError("/user", HttpStatus.UNPROCESSABLE_ENTITY, "same_password");

        assertResponseStatus(() -> client.updatePassword("acc", "senha"),
            HttpStatus.BAD_REQUEST, "A nova senha deve ser diferente da atual");
    }

    @Test
    void updatePasswordComSenhaFracaLanca400() {
        respondError("/user", HttpStatus.UNPROCESSABLE_ENTITY, "weak_password");

        assertResponseStatus(() -> client.updatePassword("acc", "123"),
            HttpStatus.BAD_REQUEST, "A senha não atende aos requisitos mínimos de segurança");
    }

    @Test
    void recoverAntesDoIntervaloMinimoLanca429() {
        respondError("/recover", HttpStatus.TOO_MANY_REQUESTS, "over_email_send_rate_limit");

        assertResponseStatus(() -> client.recover("ana@email.com"),
            HttpStatus.TOO_MANY_REQUESTS, "Aguarde um pouco antes de pedir um novo código");
    }

    @Test
    void passwordGrantComExcessoDeTentativasLanca429() {
        respondError("/token?grant_type=password", HttpStatus.TOO_MANY_REQUESTS, "over_request_rate_limit");

        assertResponseStatus(() -> client.passwordGrant("ana@email.com", "senha"),
            HttpStatus.TOO_MANY_REQUESTS, "Muitas tentativas. Aguarde um pouco e tente novamente");
    }
}
