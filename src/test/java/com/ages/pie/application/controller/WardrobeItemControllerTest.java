package com.ages.pie.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.ages.pie.application.dto.wardrobe.WardrobeItemResponseDTO;
import com.ages.pie.application.dto.wardrobe.WardrobeImageAnalysisDTO;
import com.ages.pie.application.service.WardrobeImageAnalysisService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.ages.pie.application.service.WardrobeItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

@WebMvcTest(WardrobeItemController.class)
class WardrobeItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WardrobeItemService wardrobeItemService;

    @MockitoBean
    private WardrobeImageAnalysisService analysisService;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Test
    void analyzesPhotoForAuthenticatedUser() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(UUID.randomUUID());
        when(analysisService.analyze(any())).thenReturn(
                new WardrobeImageAnalysisDTO("camisa", "casual", "azul"));
        mockMvc.perform(multipart("/users/me/wardrobe/items/analyze")
                        .file(new MockMultipartFile("file", "camisa.jpg", "image/jpeg", new byte[] {1})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("camisa"))
                .andExpect(jsonPath("$.style").value("casual"))
                .andExpect(jsonPath("$.color").value("azul"));
        verifyNoInteractions(wardrobeItemService);
    }

    @Test
    void rejectsAnalysisWithoutAuthentication() throws Exception {
        when(authenticatedUserProvider.id()).thenThrow(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado"));
        mockMvc.perform(multipart("/users/me/wardrobe/items/analyze")
                        .file(new MockMultipartFile("file", "camisa.jpg", "image/jpeg", new byte[] {1})))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(analysisService, wardrobeItemService);
    }

    @Test
    void requiresPhotoForAnalysis() throws Exception {
        mockMvc.perform(multipart("/users/me/wardrobe/items/analyze"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(analysisService);
    }

    @Test
    void exposesRecoverableAnalysisError() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(UUID.randomUUID());
        when(analysisService.analyze(any())).thenThrow(new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY, "Não identificamos uma peça. Envie outra foto."));
        mockMvc.perform(multipart("/users/me/wardrobe/items/analyze")
                        .file(new MockMultipartFile("file", "camisa.jpg", "image/jpeg", new byte[] {1})))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Não identificamos uma peça. Envie outra foto."));
    }

    @Test
    void createRetorna201ComDadosDaPecaCriada() throws Exception {
        UUID id = UUID.randomUUID();
        when(wardrobeItemService.create(any(), any())).thenReturn(
                new WardrobeItemResponseDTO(
                        id,
                        null,
                        "Camisa",
                        "camisa",
                        "casual",
                        "azul",
                        "https://storage.test/camisa.jpg"));

        MockMultipartFile item = new MockMultipartFile(
                "item",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Camisa\",\"category\":\"camisa\",\"style\":\"casual\",\"color\":\"azul\"}"
                        .getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = new MockMultipartFile(
                "file", "camisa.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/users/me/wardrobe/items")
                        .file(item)
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Camisa"))
                .andExpect(jsonPath("$.style").value("casual"))
                .andExpect(jsonPath("$.photoUrl").value("https://storage.test/camisa.jpg"));
    }

    @Test
    void createRetorna400QuandoNomeNaoForInformado() throws Exception {
        MockMultipartFile item = new MockMultipartFile(
                "item",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"category\":\"camisa\",\"style\":\"casual\"}"
                        .getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = new MockMultipartFile(
                "file", "camisa.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/users/me/wardrobe/items")
                        .file(item)
                        .file(file))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(wardrobeItemService);
    }

    @Test
    void createRetorna400QuandoEstiloNaoPertencerATaxonomia() throws Exception {
        MockMultipartFile item = new MockMultipartFile(
                "item",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Camisa\",\"category\":\"camisa\",\"style\":\"classic\"}"
                        .getBytes(StandardCharsets.UTF_8));
        MockMultipartFile file = new MockMultipartFile(
                "file", "camisa.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/users/me/wardrobe/items")
                        .file(item)
                        .file(file))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(wardrobeItemService);
    }
}
