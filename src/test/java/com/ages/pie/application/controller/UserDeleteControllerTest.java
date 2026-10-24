package com.ages.pie.application.controller;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class})
@WithMockUser
class UserDeleteControllerTest {

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
    void deletesOwnAccount() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authProvider.id()).thenReturn(userId);

        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isNoContent());

        verify(userService).delete(userId);
    }

    @Test
    void forbidsDeletingAnotherAccount() throws Exception {
        when(authProvider.id()).thenReturn(UUID.randomUUID());

        mockMvc.perform(delete("/users/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());

        verify(userService, never()).delete(any());
    }

    @WithAnonymousUser
    @Test
    void rejectsRequestWithoutToken() throws Exception {
        when(authProvider.id()).thenThrow(new ResponseStatusException(UNAUTHORIZED, "Usuário não autenticado"));

        mockMvc.perform(delete("/users/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).delete(any());
    }
}
