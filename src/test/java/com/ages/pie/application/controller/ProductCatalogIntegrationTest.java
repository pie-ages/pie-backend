package com.ages.pie.application.controller;

import com.ages.pie.application.config.SecurityConfig;
import com.ages.pie.application.config.SupabaseAuthProperties;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.application.service.ProductService;
import com.ages.pie.application.service.PreferenceService;
import com.ages.pie.application.service.RecommendationService;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ProductStatus;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.ProductImageRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import com.ages.pie.infrastructure.repository.WishlistItemRepository;
import com.ages.pie.infrastructure.repository.WishlistRepository;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import com.ages.pie.infrastructure.service.HeuristicRecommendationScorer;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, SupabaseAuthProperties.class, ProductService.class,
        RecommendationService.class, ProductMapper.class, HeuristicRecommendationScorer.class})
class ProductCatalogIntegrationTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean PreferenceService preferenceService;
    @MockitoBean ProductRepository productRepository;
    @MockitoBean UserRepository userRepository;
    @MockitoBean BodyProfileRepository bodyProfileRepository;
    @MockitoBean CompanyRepository companyRepository;
    @MockitoBean ProductImageRepository productImageRepository;
    @MockitoBean WishlistRepository wishlistRepository;
    @MockitoBean WishlistItemRepository wishlistItemRepository;
    @MockitoBean AuthenticatedUserProvider authenticatedUserProvider;
    @MockitoBean JwtDecoder jwtDecoder;

    @Test
    void authenticatedCatalogReturnsRankedPagesAndPartnerStoreData() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.optionalId()).thenReturn(Optional.of(userId));
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getStyles()).thenReturn(List.of(ProductStyle.CASUAL));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Product older = product("Mais antiga", OffsetDateTime.now().minusDays(1));
        Product newer = product("Mais recente", OffsetDateTime.now());
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED)).thenReturn(List.of(older, newer));

        mockMvc.perform(get("/products").with(jwt()).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.items[0].id").value(newer.getId().toString()))
                .andExpect(jsonPath("$.items[0].name").value("Mais recente"))
                .andExpect(jsonPath("$.items[0].price").value(49.90))
                .andExpect(jsonPath("$.items[0].imageUrl").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.items[0].purchaseUrl").value("https://example.com/buy"))
                .andExpect(jsonPath("$.items[0].companyName").value("Loja parceira"));
        mockMvc.perform(get("/products").with(jwt()).param("size", "1").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.items[0].id").value(older.getId().toString()));
        verify(productRepository, never()).findCatalog(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void emptyRecommendationsReturnStandardCatalog() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.optionalId()).thenReturn(Optional.of(userId));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        stubStandardCatalog();

        expectStandardCatalog();

        verify(productRepository, never()).findByActiveTrueAndStatus(any());
    }

    @Test
    void recommendationFailureReturnsStandardCatalogInsteadOfServerError() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authenticatedUserProvider.optionalId()).thenReturn(Optional.of(userId));
        when(userRepository.findById(userId)).thenThrow(new DataAccessResourceFailureException("Profile unavailable"));
        stubStandardCatalog();

        expectStandardCatalog();
    }

    @Test
    void anonymousCatalogRemainsPublic() throws Exception {
        stubStandardCatalog();

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Catálogo padrão"));

        verify(userRepository, never()).findById(any());
    }

    private void stubStandardCatalog() {
        var page = new PageImpl<>(List.of(product("Catálogo padrão", OffsetDateTime.now())), PageRequest.of(0, 20), 1);
        when(productRepository.findCatalog(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(page);
    }

    private void expectStandardCatalog() throws Exception {
        mockMvc.perform(get("/products").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.items[0].name").value("Catálogo padrão"));
    }

    private Product product(String name, OffsetDateTime createdAt) {
        Company company = mock(Company.class);
        when(company.getName()).thenReturn("Loja parceira");
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(UUID.randomUUID());
        when(product.getName()).thenReturn(name);
        when(product.getCompany()).thenReturn(company);
        when(product.getCreatedAt()).thenReturn(createdAt);
        when(product.getStyles()).thenReturn(List.of("casual"));
        when(product.getPrice()).thenReturn(new BigDecimal("49.90"));
        when(product.getImageUrl()).thenReturn("https://example.com/image.jpg");
        when(product.getPurchaseUrl()).thenReturn("https://example.com/buy");
        when(product.getStatus()).thenReturn(ProductStatus.PUBLISHED);
        return product;
    }
}
