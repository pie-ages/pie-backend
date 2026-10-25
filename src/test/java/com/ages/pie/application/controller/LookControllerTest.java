package com.ages.pie.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.dto.look.LookItemDTO;
import com.ages.pie.application.dto.look.LookItemRefDTO;
import com.ages.pie.application.dto.look.LookRequestDTO;
import com.ages.pie.application.dto.look.LookResponseDTO;
import com.ages.pie.application.service.LookService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Garante o contrato HTTP de criação de look: o JSON enviado pelo app (campo {@code items}, com um
 * único id por peça) chega ao serviço como {@link LookRequestDTO} na mesma ordem, e a resposta
 * serializa as peças na ordem devolvida pelo serviço.
 */
@WebMvcTest(LookController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class})
@WithMockUser
class LookControllerTest {

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LookService lookService;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    private final UUID userId = UUID.randomUUID();

    @Test
    void create_shouldBindItemsInRequestOrder_whenJsonHasOnlyOneIdPerItem() throws Exception {
        UUID casaco = UUID.randomUUID();
        UUID camisa = UUID.randomUUID();
        UUID calca = UUID.randomUUID();
        UUID tenis = UUID.randomUUID();
        when(authenticatedUserProvider.id()).thenReturn(userId);
        when(lookService.create(any(), any())).thenReturn(response(List.of(
                item(null, casaco, "Casaco"),
                item(camisa, null, "Camisa"),
                item(null, calca, "Calça"),
                item(tenis, null, "Tênis"))));

        // Mesmo formato enviado pelo frontend: cada item traz só wardrobeItemId OU productId.
        String body = """
                {"title":"Look","occasion":"Trabalho","items":[
                  {"productId":"%s"},
                  {"wardrobeItemId":"%s"},
                  {"productId":"%s"},
                  {"wardrobeItemId":"%s"}
                ]}""".formatted(casaco, camisa, calca, tenis);

        mockMvc.perform(post("/users/me/looks").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].name").value("Casaco"))
                .andExpect(jsonPath("$.items[1].name").value("Camisa"))
                .andExpect(jsonPath("$.items[2].name").value("Calça"))
                .andExpect(jsonPath("$.items[3].name").value("Tênis"));

        ArgumentCaptor<LookRequestDTO> captor = ArgumentCaptor.forClass(LookRequestDTO.class);
        verify(lookService).create(eq(userId), captor.capture());
        LookRequestDTO dto = captor.getValue();
        assertThat(dto.title()).isEqualTo("Look");
        assertThat(dto.wardrobeItemIds()).isNull();
        assertThat(dto.productIds()).isNull();
        assertThat(dto.items()).containsExactly(
                new LookItemRefDTO(null, casaco),
                new LookItemRefDTO(camisa, null),
                new LookItemRefDTO(null, calca),
                new LookItemRefDTO(tenis, null));
    }

    @Test
    void create_shouldStillBindLegacyLists_whenItemsIsAbsent() throws Exception {
        UUID wardrobeItem = UUID.randomUUID();
        UUID product = UUID.randomUUID();
        when(authenticatedUserProvider.id()).thenReturn(userId);
        when(lookService.create(any(), any())).thenReturn(response(List.of()));

        String body = """
                {"title":"Look","wardrobeItemIds":["%s"],"productIds":["%s"]}""".formatted(wardrobeItem, product);

        mockMvc.perform(post("/users/me/looks").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        ArgumentCaptor<LookRequestDTO> captor = ArgumentCaptor.forClass(LookRequestDTO.class);
        verify(lookService).create(eq(userId), captor.capture());
        assertThat(captor.getValue().items()).isNull();
        assertThat(captor.getValue().wardrobeItemIds()).containsExactly(wardrobeItem);
        assertThat(captor.getValue().productIds()).containsExactly(product);
    }

    @Test
    void create_shouldRejectNullItemElement_withBadRequest() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(userId);

        mockMvc.perform(post("/users/me/looks").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Look","items":[null]}"""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(lookService);
    }

    @Test
    void create_shouldRejectBlankTitle_withBadRequest() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(userId);

        mockMvc.perform(post("/users/me/looks").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" ","items":[{"wardrobeItemId":"%s"}]}""".formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(lookService);
    }

    private static LookItemDTO item(UUID wardrobeItemId, UUID productId, String name) {
        return new LookItemDTO(wardrobeItemId, productId, name, null, null, null);
    }

    private static LookResponseDTO response(List<LookItemDTO> items) {
        OffsetDateTime now = OffsetDateTime.now();
        return new LookResponseDTO(UUID.randomUUID(), "Look", null, "Trabalho", null, false, items, now, now);
    }
}
