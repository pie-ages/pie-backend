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
