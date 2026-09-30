package com.ages.pie.application.controller;

import com.ages.pie.application.dto.user.StyleIdentificationResponseDTO;
import com.ages.pie.application.service.PreferenceService;
import com.ages.pie.application.service.StyleIdentificationService;
import com.ages.pie.application.service.UserService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserStyleIdentificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PreferenceService preferenceService;

    @MockitoBean
    private StyleIdentificationService styleIdentificationService;

    @MockitoBean
    private AuthenticatedUserProvider authProvider;

    @Test
        void identifiesAuthenticatedUserFromPersistedAnswers() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authProvider.id()).thenReturn(userId);
        when(styleIdentificationService.identify(userId))
                .thenReturn(new StyleIdentificationResponseDTO(List.of("ELEGANTE")));

        mockMvc.perform(post("/users/me/style/identify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.styles[0]").value("ELEGANTE"));

        verify(styleIdentificationService).identify(userId);
    }

    @Test
        void returnsEmptyListWithoutData() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authProvider.id()).thenReturn(userId);
        when(styleIdentificationService.identify(userId))
                .thenReturn(new StyleIdentificationResponseDTO(List.of()));

        mockMvc.perform(post("/users/me/style/identify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.styles").isEmpty());
    }

    @Test
        void rejectsUnauthenticatedUser() throws Exception {
        when(authProvider.id()).thenThrow(new ResponseStatusException(UNAUTHORIZED, "Usuário não autenticado"));

        mockMvc.perform(post("/users/me/style/identify"))
                .andExpect(status().isUnauthorized());
    }

        @Test
        void readsPersistedResultWithoutRunningIdentification() throws Exception {
                UUID userId = UUID.randomUUID();
                when(authProvider.id()).thenReturn(userId);
                when(styleIdentificationService.getIdentified(userId))
                                .thenReturn(new StyleIdentificationResponseDTO(List.of("BOHO")));

                mockMvc.perform(get("/users/me/style/identified"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.styles[0]").value("BOHO"));

                verify(styleIdentificationService).getIdentified(userId);
        }
}