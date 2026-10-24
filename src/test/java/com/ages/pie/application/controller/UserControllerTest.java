package com.ages.pie.application.controller;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.dto.user.UserResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.service.PreferenceService;
import com.ages.pie.application.service.StyleIdentificationService;
import com.ages.pie.application.service.UserService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class})
@WithMockUser
class UserControllerTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

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
    void me_shouldReturnAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-10T12:00:00Z");
        when(authProvider.id()).thenReturn(userId);
        when(userService.findById(userId)).thenReturn(new UserResponseDTO(
                userId, "Ana", "ana@pie.com", "https://cdn/ana.png", createdAt, createdAt));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@pie.com"))
                .andExpect(jsonPath("$.photoUrl").value("https://cdn/ana.png"));

        verify(userService).findById(userId);
    }

    @WithAnonymousUser
    @Test
    void me_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuário não autenticado"));

        verify(userService, never()).findById(any());
    }

    @Test
    void me_shouldReturn404_whenAuthenticatedUserHasNoLocalRecord() throws Exception {
        UUID supabaseUserId = UUID.randomUUID();
        when(authProvider.id()).thenThrow(
                new ResourceNotFoundException("Usuário autenticado sem cadastro: " + supabaseUserId));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário autenticado sem cadastro: " + supabaseUserId));

        verify(userService, never()).findById(any());
    }

    @Test
    void me_shouldReturn404_whenUserDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authProvider.id()).thenReturn(userId);
        when(userService.findById(userId))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado: " + userId));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuário não encontrado: " + userId));
    }

    @Test
    void findById_shouldStillResolveUuidPath() throws Exception {
        UUID otherId = UUID.randomUUID();
        when(userService.findById(otherId)).thenReturn(new UserResponseDTO(
                otherId, "Bia", "bia@pie.com", null, null, null));

        mockMvc.perform(get("/users/{id}", otherId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(otherId.toString()));

        verify(userService).findById(otherId);
        verify(authProvider, never()).id();
    }
}
