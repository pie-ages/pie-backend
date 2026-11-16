package com.ages.pie.application.controller;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.dto.company.CompanyResponseDTO;
import com.ages.pie.application.service.CompanyService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class})
class AdminControllerTest {

    private static final String VALID_BODY =
            "{\"name\":\"Admin Novo\",\"email\":\"admin@pie.com\",\"password\":\"senha123\"}";

    @MockitoBean private JwtDecoder jwtDecoder;
    @MockitoBean private CompanyService companyService;
    @MockitoBean private AuthenticatedUserProvider authProvider;

    @Autowired private MockMvc mockMvc;

    @Test
    void criarAdminExigeToken() throws Exception {
        mockMvc.perform(post("/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verify(companyService, never()).createAdmin(any());
    }

    @Test
    void criarAdminRetorna201ComPerfilAdmin() throws Exception {
        when(companyService.createAdmin(any())).thenReturn(
                new CompanyResponseDTO(UUID.randomUUID(), "Admin Novo", null, null, null,
                        "admin@pie.com", null, true, null, "ADMIN", "ACTIVE", null, null));

        mockMvc.perform(post("/admins")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void criarAdminRetorna400QuandoBodyInvalido() throws Exception {
        mockMvc.perform(post("/admins")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"invalido\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(companyService, never()).createAdmin(any());
    }

    @Test
    void criarAdminRetorna403QuandoUsuarioNaoEhAdmin() throws Exception {
        when(companyService.createAdmin(any())).thenThrow(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito ao administrador"));

        mockMvc.perform(post("/admins")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }
}
