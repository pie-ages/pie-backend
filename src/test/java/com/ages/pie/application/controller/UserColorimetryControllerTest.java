package com.ages.pie.application.controller;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.dto.user.ColorimetrySeasonDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.application.service.ColorimetryService;
import com.ages.pie.application.service.PreferenceService;
import com.ages.pie.application.service.StyleIdentificationService;
import com.ages.pie.application.service.UserService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class})
@WithMockUser
class UserColorimetryControllerTest {
    @MockitoBean JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PreferenceService preferenceService;

    @MockitoBean
    private ColorimetryService colorimetryService;

    @MockitoBean
    private StyleIdentificationService styleIdentificationService;

    @MockitoBean
    private AuthenticatedUserProvider authProvider;

    @Test
    void analyzesPhotoForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authProvider.id()).thenReturn(userId);
        when(colorimetryService.analyze(eq(userId), any())).thenReturn(
                new PreferencesResponseDTO(List.of("#FFB58C"), List.of("#00C48C"), List.of("#AABBCC"),
                        new ColorimetrySeasonDTO("soft_autumn", "Outono Suave", "Tons quentes e suaves valorizam sua pele.",
                                "Tons frios e vivos competem com a sua coloração natural.")));

        mockMvc.perform(multipart("/users/me/colorimetry")
                        .file(new MockMultipartFile("file", "face.jpg", "image/jpeg", new byte[] {1})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highlightColors[0]").value("#FFB58C"))
                .andExpect(jsonPath("$.avoidColors[0]").value("#00C48C"))
                .andExpect(jsonPath("$.favoriteColors[0]").value("#AABBCC"))
                .andExpect(jsonPath("$.season.label").value("Outono Suave"))
                .andExpect(jsonPath("$.season.avoidCaption").value("Tons frios e vivos competem com a sua coloração natural."));
    }
}
