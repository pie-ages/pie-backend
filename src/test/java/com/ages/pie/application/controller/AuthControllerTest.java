package com.ages.pie.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.ages.pie.application.dto.LoginResponseDTO;
import com.ages.pie.application.dto.UserSummaryDTO;
import com.ages.pie.application.exception.NoUsersAvailableException;
import com.ages.pie.application.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginRetorna200ComTokenEUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        when(authService.login(any())).thenReturn(
            new LoginResponseDTO("tok-abc", new UserSummaryDTO(id, "Ana", "ana@email.com", null)));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@email.com\",\"password\":\"qualquer\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.token").value("tok-abc"))
            .andExpect(jsonPath("$.user.id").value(id.toString()))
            .andExpect(jsonPath("$.user.name").value("Ana"))
            .andExpect(jsonPath("$.user.email").value("ana@email.com"))
            .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void loginRetorna404QuandoNaoHaUsuarios() throws Exception {
        when(authService.login(any()))
            .thenThrow(new NoUsersAvailableException("Nenhum usuário cadastrado"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@b.com\",\"password\":\"x\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Nenhum usuário cadastrado"));
    }

    @Test
    void loginRetorna400ParaJsonMalformado() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginAceitaRequestSemPassword() throws Exception {
        when(authService.login(any())).thenReturn(
            new LoginResponseDTO("tok", new UserSummaryDTO(UUID.randomUUID(), "Ana", "ana@email.com", null)));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@email.com\"}"))
            .andExpect(status().isOk());
    }
}
