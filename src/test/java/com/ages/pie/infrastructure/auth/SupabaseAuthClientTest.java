package com.ages.pie.infrastructure.auth;

import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SupabaseAuthClientTest {

    private static final String ADMIN_USERS_URL = "https://test.supabase.co/auth/v1/admin/users";

    private SupabaseAuthProperties props;
    private MockRestServiceServer server;
    private SupabaseAuthClient client;

    @BeforeEach
    void setUp() {
        props = new SupabaseAuthProperties();
        props.setUrl("https://test.supabase.co");
        props.setServiceRoleKey("test-key");

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SupabaseAuthClient(props, builder.build());
    }

    @Test
    void createUser_shouldReturnSupabaseId_whenCreationSucceeds() {
        UUID expectedId = UUID.randomUUID();
        server.expect(requestTo(ADMIN_USERS_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "test-key"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andExpect(jsonPath("$.password").value("senha123"))
                .andExpect(jsonPath("$.email_confirm").value(true))
                .andRespond(withSuccess(
                        "{\"id\":\"" + expectedId + "\",\"email\":\"ana@email.com\"}",
                        MediaType.APPLICATION_JSON));

        UUID id = client.createUser("ana@email.com", "senha123");

        assertThat(id).isEqualTo(expectedId);
        server.verify();
    }

    @Test
    void createUser_shouldThrowBusinessException_whenEmailAlreadyExists() {
        server.expect(requestTo(ADMIN_USERS_URL))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":422,\"error_code\":\"email_exists\",\"msg\":\"already registered\"}"));

        assertThatThrownBy(() -> client.createUser("ana@email.com", "senha123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ana@email.com");
        server.verify();
    }

    @Test
    void createUser_shouldThrowBusinessException_whenSupabaseReportsDuplicateWithoutErrorCode() {
        server.expect(requestTo(ADMIN_USERS_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":400,\"msg\":\"User already registered\"}"));

        assertThatThrownBy(() -> client.createUser("ana@email.com", "senha123"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ana@email.com");
        server.verify();
    }

    @Test
    void createUser_shouldThrowIllegalArgument_whenSupabaseRejectsThePassword() {
        server.expect(requestTo(ADMIN_USERS_URL))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":422,\"error_code\":\"weak_password\","
                                + "\"msg\":\"Password should be at least 6 characters\"}"));

        assertThatThrownBy(() -> client.createUser("ana@email.com", "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A senha não atende aos requisitos mínimos de segurança");
        server.verify();
    }

    @Test
    void createUser_shouldThrowBadGateway_whenServiceRoleKeyIsRejected() {
        server.expect(requestTo(ADMIN_USERS_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\":\"Invalid API key\"}"));

        assertThatThrownBy(() -> client.createUser("ana@email.com", "senha123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
        server.verify();
    }

    @Test
    void createUser_shouldThrowBadGateway_whenResponseHasNoId() {
        server.expect(requestTo(ADMIN_USERS_URL))
                .andRespond(withSuccess("{\"email\":\"ana@email.com\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.createUser("ana@email.com", "senha123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
        server.verify();
    }

    @Test
    void createUser_shouldThrowServiceUnavailable_whenSupabaseIsNotConfigured() {
        props.setServiceRoleKey("");

        assertThatThrownBy(() -> client.createUser("ana@email.com", "senha123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        server.verify();
    }

    @Test
    void updateEmail_shouldCallAdminEndpoint() {
        UUID id = UUID.randomUUID();
        server.expect(requestTo(ADMIN_USERS_URL + "/" + id))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("apikey", "test-key"))
                .andExpect(jsonPath("$.email").value("novo@lojay.com"))
                .andExpect(jsonPath("$.email_confirm").value(true))
                .andRespond(withSuccess());

        client.updateEmail(id, "novo@lojay.com");

        server.verify();
    }

    @Test
    void updateEmail_shouldThrowBusinessException_whenNewEmailIsTaken() {
        UUID id = UUID.randomUUID();
        server.expect(requestTo(ADMIN_USERS_URL + "/" + id))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":422,\"error_code\":\"email_exists\",\"msg\":\"already registered\"}"));

        assertThatThrownBy(() -> client.updateEmail(id, "novo@lojay.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("novo@lojay.com");
        server.verify();
    }

    @Test
    void deleteUser_shouldCallAdminEndpoint() {
        UUID id = UUID.randomUUID();
        server.expect(requestTo(ADMIN_USERS_URL + "/" + id))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("apikey", "test-key"))
                .andRespond(withSuccess());

        client.deleteUser(id);

        server.verify();
    }

    @Test
    void deleteUser_shouldSucceed_whenUserNoLongerExists() {
        UUID id = UUID.randomUUID();
        server.expect(requestTo(ADMIN_USERS_URL + "/" + id))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\":\"User not found\"}"));

        client.deleteUser(id);

        server.verify();
    }

    @Test
    void deleteUser_shouldThrowBadGateway_whenSupabaseFails() {
        UUID id = UUID.randomUUID();
        server.expect(requestTo(ADMIN_USERS_URL + "/" + id))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.deleteUser(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
        server.verify();
    }
}
