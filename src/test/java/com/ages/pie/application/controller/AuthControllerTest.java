package com.ages.pie.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.dto.UserSummaryDTO;
import com.ages.pie.application.exception.AuthProviderUnavailableException;
import com.ages.pie.application.exception.EmailNotConfirmedException;
import com.ages.pie.application.exception.InvalidCredentialsException;
import com.ages.pie.application.service.AuthService;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class, AuthenticatedUserProvider.class})
class AuthControllerTest {

    private static final String LOGIN_BODY = "{\"email\":\"ana@email.com\",\"password\":\"senha\"}";
    private static final String RECOVER_BODY = "{\"email\":\"ana@email.com\"}";
    private static final String VERIFY_BODY = "{\"email\":\"ana@email.com\",\"code\":\"123456\"}";
    private static final String PASSWORD_BODY = "{\"password\":\"nova-senha\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CompanyRepository companyRepository;

    @Test
    void loginRetorna200ComTokensEUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        when(authService.loginCustomer(any())).thenReturn(new LoginResponseDTO(
            "acc", "ref", 3600, "bearer", new UserSummaryDTO(id, "Ana", "ana@email.com", null, null, null)));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value("acc"))
            .andExpect(jsonPath("$.refreshToken").value("ref"))
            .andExpect(jsonPath("$.expiresIn").value(3600))
            .andExpect(jsonPath("$.tokenType").value("bearer"))
            .andExpect(jsonPath("$.user.id").value(id.toString()))
            .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void loginComCredenciaisInvalidasRetorna401() throws Exception {
        when(authService.loginCustomer(any())).thenThrow(new InvalidCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    void loginComEmailNaoConfirmadoRetorna403() throws Exception {
        when(authService.loginCustomer(any())).thenThrow(new EmailNotConfirmedException("E-mail ainda não confirmado"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
            .andExpect(status().isForbidden());
    }

    @Test
    void loginComSupabaseIndisponivelRetorna503() throws Exception {
        when(authService.loginCustomer(any())).thenThrow(new AuthProviderUnavailableException("indisponível"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    void loginSemPasswordRetorna400() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@email.com\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginRetorna400ParaJsonMalformado() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void refreshRetornaNovaSessao() throws Exception {
        when(authService.refresh("ref")).thenReturn(new LoginResponseDTO("acc2", "ref2", 3600, "bearer", null));

        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"ref\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("acc2"))
            .andExpect(jsonPath("$.refreshToken").value("ref2"));
    }

    @Test
    void refreshSemTokenRetorna400() throws Exception {
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void logoutAutenticadoRetorna204ERevogaSessao() throws Exception {
        mockMvc.perform(post("/auth/logout").with(jwt().jwt(j -> j.tokenValue("acc"))))
            .andExpect(status().isNoContent());

        verify(authService).logout("acc");
    }

    @Test
    void logoutSemTokenRetorna401() throws Exception {
        mockMvc.perform(post("/auth/logout"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void meRetornaUsuarioDoToken() throws Exception {
        UUID id = UUID.randomUUID();
        UUID supabaseUserId = UUID.randomUUID();
        User user = new User("Ana", "ana@email.com", supabaseUserId);
        ReflectionTestUtils.setField(user, "id", id);
        when(userRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.of(user));
        when(authService.findSummary(id)).thenReturn(new UserSummaryDTO(id, "Ana", "ana@email.com", null, null, null));

        mockMvc.perform(get("/auth/me").with(jwt().jwt(j -> j.subject(supabaseUserId.toString()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Ana"));
    }

    @Test
    void meRetorna404QuandoCustomerNaoCadastrado() throws Exception {
        UUID supabaseUserId = UUID.randomUUID();
        when(userRepository.findBySupabaseUserId(supabaseUserId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/auth/me").with(jwt().jwt(j -> j.subject(supabaseUserId.toString()))))
            .andExpect(status().isNotFound());
    }

    @Test
    void recoverRetorna204() throws Exception {
        mockMvc.perform(post("/auth/recover").contentType(MediaType.APPLICATION_JSON).content(RECOVER_BODY))
            .andExpect(status().isNoContent());

        verify(authService).recover("ana@email.com");
    }

    @Test
    void recoverComEmailInvalidoRetorna400() throws Exception {
        mockMvc.perform(post("/auth/recover").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nao-e-email\"}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void recoverAntesDoIntervaloMinimoRetorna429ComMensagem() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Aguarde um pouco antes de pedir um novo código"))
            .when(authService).recover("ana@email.com");

        mockMvc.perform(post("/auth/recover").contentType(MediaType.APPLICATION_JSON).content(RECOVER_BODY))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.message").value("Aguarde um pouco antes de pedir um novo código"));
    }

    @Test
    void verifyRecoveryRetorna200ComSessao() throws Exception {
        UUID id = UUID.randomUUID();
        when(authService.verifyRecovery("ana@email.com", "123456")).thenReturn(new LoginResponseDTO(
            "acc", "ref", 3600, "bearer", new UserSummaryDTO(id, "Ana", "ana@email.com", null, null, null)));

        mockMvc.perform(post("/auth/recover/verify").contentType(MediaType.APPLICATION_JSON).content(VERIFY_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("acc"))
            .andExpect(jsonPath("$.refreshToken").value("ref"))
            .andExpect(jsonPath("$.user.id").value(id.toString()));
    }

    @Test
    void verifyRecoveryComCodigoForaDoFormatoRetorna400() throws Exception {
        mockMvc.perform(post("/auth/recover/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@email.com\",\"code\":\"12ab\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("code: O código deve ter 6 dígitos"));

        verifyNoInteractions(authService);
    }

    @Test
    void verifyRecoveryComCodigoInvalidoRetorna400ComMensagem() throws Exception {
        when(authService.verifyRecovery("ana@email.com", "123456"))
            .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado"));

        mockMvc.perform(post("/auth/recover/verify").contentType(MediaType.APPLICATION_JSON).content(VERIFY_BODY))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Código inválido ou expirado"));
    }

    @Test
    void updatePasswordAutenticadoRetorna204EPassaOToken() throws Exception {
        mockMvc.perform(put("/auth/password").with(jwt().jwt(j -> j.tokenValue("acc")))
                .contentType(MediaType.APPLICATION_JSON).content(PASSWORD_BODY))
            .andExpect(status().isNoContent());

        verify(authService).updatePassword("acc", "nova-senha");
    }

    @Test
    void updatePasswordSemTokenRetorna401() throws Exception {
        mockMvc.perform(put("/auth/password").contentType(MediaType.APPLICATION_JSON).content(PASSWORD_BODY))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }

    @Test
    void updatePasswordForaDaRecuperacaoRetorna403ComMensagem() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Troca de senha disponível apenas pela recuperação de acesso"))
            .when(authService).updatePassword(any(), eq("nova-senha"));

        mockMvc.perform(put("/auth/password").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content(PASSWORD_BODY))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Troca de senha disponível apenas pela recuperação de acesso"));
    }

    @Test
    void updatePasswordComSenhaIgualRetorna400ComMensagem() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nova senha deve ser diferente da atual"))
            .when(authService).updatePassword(any(), eq("nova-senha"));

        mockMvc.perform(put("/auth/password").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content(PASSWORD_BODY))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("A nova senha deve ser diferente da atual"));
    }
}
