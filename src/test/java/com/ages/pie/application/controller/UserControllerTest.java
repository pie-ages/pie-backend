package com.ages.pie.application.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.ages.pie.application.dto.user.UserResponseDTO;
import com.ages.pie.application.service.PreferenceService;
import com.ages.pie.application.service.StyleIdentificationService;
import com.ages.pie.application.service.UserService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

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
    void returnsAuthenticatedUserProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authProvider.id()).thenReturn(userId);
        when(userService.findById(userId)).thenReturn(
                new UserResponseDTO(userId, "Ana Silva", "ana@email.com", null, null, null));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Ana Silva"))
                .andExpect(jsonPath("$.email").value("ana@email.com"));

        verify(userService).findById(userId);
    }
}
