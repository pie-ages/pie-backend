package com.ages.pie.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.dto.UserSummaryDTO;
import com.ages.pie.application.exception.AuthProviderUnavailableException;
import com.ages.pie.application.exception.EmailNotConfirmedException;
import com.ages.pie.application.exception.InvalidCredentialsException;
import com.ages.pie.application.service.AuthService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class, AuthenticatedUserProvider.class})
class AuthControllerTest {

    private static final String LOGIN_BODY = "{\"email\":\"ana@email.com\",\"password\":\"senha\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void loginRetorna200ComTokensEUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        when(authService.login(any())).thenReturn(new LoginResponseDTO(
            "acc", "ref", 3600, "bearer", new UserSummaryDTO(id, "Ana", "ana@email.com", null)));

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
        when(authService.login(any())).thenThrow(new InvalidCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    void loginComEmailNaoConfirmadoRetorna403() throws Exception {
        when(authService.login(any())).thenThrow(new EmailNotConfirmedException("E-mail ainda não confirmado"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
            .andExpect(status().isForbidden());
    }

    @Test
    void loginComSupabaseIndisponivelRetorna503() throws Exception {
        when(authService.login(any())).thenThrow(new AuthProviderUnavailableException("indisponível"));

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
        when(authService.findSummary(id)).thenReturn(new UserSummaryDTO(id, "Ana", "ana@email.com", null));

        mockMvc.perform(get("/auth/me").with(jwt().jwt(j -> j.subject(id.toString()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Ana"));
    }

    @Test
    void meRetorna404QuandoCustomerNaoCadastrado() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/auth/me").with(jwt().jwt(j -> j.subject(id.toString()))))
            .andExpect(status().isNotFound());
    }
}
