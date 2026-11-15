package com.ages.pie.application.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.dto.RecommendedProductDTO;
import com.ages.pie.application.service.RecommendationService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class})
@WithMockUser
class ProductRecommendationTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean RecommendationService recommendationService;
    @MockitoBean AuthenticatedUserProvider authenticatedUserProvider;
    @MockitoBean com.ages.pie.application.service.ProductService productService;
    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    void returnsRecommendationsForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.id()).thenReturn(userId);
        when(recommendationService.recommendForUser(eq(userId))).thenReturn(List.of(
                new RecommendedProductDTO(UUID.randomUUID(), "casual tee", "camiseta",
                        "azul", new BigDecimal("99.90"), null, 100.0)));

        mockMvc.perform(get("/products/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("casual tee"))
                .andExpect(jsonPath("$[0].score").value(100.0));
    }

    @Test
    void forwardsLimitWhenProvided() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.id()).thenReturn(userId);
        when(recommendationService.recommendForUser(eq(userId), eq(1))).thenReturn(List.of());

        mockMvc.perform(get("/products/recommendations").param("limit", "1"))
                .andExpect(status().isOk());

        verify(recommendationService).recommendForUser(userId, 1);
    }

    @WithAnonymousUser
    @Test
    void rejectsWithoutToken() throws Exception {
        when(authenticatedUserProvider.id())
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        mockMvc.perform(get("/products/recommendations"))
                .andExpect(status().isUnauthorized());
    }
}
