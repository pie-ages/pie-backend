package com.ages.pie.application.service;

import com.ages.pie.application.dto.RecommendedProductDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ProductStatus;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.service.HeuristicRecommendationScorer;
import com.ages.pie.infrastructure.repository.ProductRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BodyProfileRepository bodyProfileRepository = mock(BodyProfileRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ProductMapper productMapper = new ProductMapper(null);
    private final RecommendationService service = new RecommendationService(
            userRepository, bodyProfileRepository, productRepository, new HeuristicRecommendationScorer(), productMapper);

    private final UUID userId = UUID.randomUUID();
    private final User user = mock(User.class);

    @BeforeEach
    void setUp() {
        when(user.getId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    private Product product(String name, List<String> styles, String color, OffsetDateTime createdAt) {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(UUID.randomUUID());
        when(product.getName()).thenReturn(name);
        when(product.getStyles()).thenReturn(styles);
        when(product.getColor()).thenReturn(color);
        when(product.getCreatedAt()).thenReturn(createdAt);
        return product;
    }

    private void givenPreferences(List<ProductStyle> styles, String identifiedStyle, String[] favoriteColors) {
        when(user.getStyles()).thenReturn(styles);
        if (identifiedStyle == null && favoriteColors == null) {
            when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.empty());
            return;
        }
        BodyProfile profile = mock(BodyProfile.class);
        when(profile.getIdentifiedStyle()).thenReturn(identifiedStyle);
        when(profile.getFavoriteColors()).thenReturn(favoriteColors);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));
    }

    @Test
    void usuarioInexistenteLancaNotFound() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.recommendForUser(UUID.randomUUID()));
    }

    @Test
    void semPreferenciasRetornaListaVaziaSemConsultarCatalogo() {
        givenPreferences(List.of(), null, null);
        List<RecommendedProductDTO> result = service.recommendForUser(userId);
        assertTrue(result.isEmpty());
    }

    @Test
    void filtraScoreZeroEOrdenaPorMaiorScore() {
        givenPreferences(List.of(ProductStyle.CASUAL), null, null);
        Product bom = product("casual tee", List.of("casual"), null, OffsetDateTime.now());
        Product ruim = product("dramatico coat", List.of("dramatico"), null, OffsetDateTime.now());
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED)).thenReturn(List.of(ruim, bom));

        List<RecommendedProductDTO> result = service.recommendForUser(userId);

        assertEquals(1, result.size());
        assertEquals(50.0, result.get(0).score(), 1e-9);
    }

    @Test
    void desempataPelaCriacaoMaisRecente() {
        givenPreferences(List.of(ProductStyle.CASUAL), null, null);
        OffsetDateTime agora = OffsetDateTime.now();
        Product antigo = product("old", List.of("casual"), null, agora.minusDays(1));
        Product novo = product("new", List.of("casual"), null, agora);
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED)).thenReturn(List.of(antigo, novo));

        List<RecommendedProductDTO> result = service.recommendForUser(userId);

        assertEquals(2, result.size());
        assertEquals("new", result.get(0).name());
        assertEquals("old", result.get(1).name());
    }

    @Test
    void estiloIdentificadoContaComoPreferenciaECorSoma() {
        givenPreferences(List.of(), "casual", new String[]{"Azul"});
        Product completo = product("full match", List.of("casual"), "azul", OffsetDateTime.now());
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED)).thenReturn(List.of(completo));

        List<RecommendedProductDTO> result = service.recommendForUser(userId);

        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).score(), 1e-9);
    }

    @Test
    void respeitaLimiteInformado() {
        givenPreferences(List.of(ProductStyle.CASUAL), null, null);
        OffsetDateTime agora = OffsetDateTime.now();
        List<Product> produtos = List.of(
                product("a", List.of("casual"), null, agora),
                product("b", List.of("casual"), null, agora.plusMinutes(1)));
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED)).thenReturn(produtos);

        List<RecommendedProductDTO> result = service.recommendForUser(userId, 1);

        assertEquals(1, result.size());
    }

    @Test
    void paginatesRankedRecommendationsWithoutLosingTotalOrRepeatingItems() {
        givenPreferences(List.of(ProductStyle.CASUAL), null, null);
        OffsetDateTime now = OffsetDateTime.now();
        List<Product> products = java.util.stream.IntStream.range(0, 25)
                .mapToObj(i -> product("item-" + i, List.of("casual"), "azul", now.plusMinutes(i)))
                .toList();
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED)).thenReturn(products);

        var first = service.recommendCatalogForUser(userId, PageRequest.of(0, 20));
        var second = service.recommendCatalogForUser(userId, PageRequest.of(1, 20));
        var beyondLast = service.recommendCatalogForUser(userId, PageRequest.of(2, 20));

        assertThat(first.total()).isEqualTo(25);
        assertThat(first.page()).isZero();
        assertThat(first.size()).isEqualTo(20);
        assertThat(first.items()).hasSize(20);
        assertThat(first.items().getFirst().name()).isEqualTo("item-24");
        assertThat(second.total()).isEqualTo(25);
        assertThat(second.page()).isEqualTo(1);
        assertThat(second.size()).isEqualTo(20);
        assertThat(second.items()).extracting(item -> item.name())
                .containsExactly("item-4", "item-3", "item-2", "item-1", "item-0");
        assertThat(second.items()).doesNotContainAnyElementsOf(first.items());
        assertThat(beyondLast.items()).isEmpty();
        assertThat(beyondLast.total()).isEqualTo(25);
    }

    @Test
    void catalogMappingPreservesProductDisplayAndPartnerStoreFields() {
        givenPreferences(List.of(ProductStyle.CASUAL), null, null);
        Product product = product("Camiseta", List.of("casual"), "azul", OffsetDateTime.now());
        var company = mock(com.ages.pie.domain.entity.Company.class);
        when(company.getName()).thenReturn("Loja parceira");
        when(product.getCompany()).thenReturn(company);
        when(product.getCategory()).thenReturn("camiseta");
        when(product.getPrice()).thenReturn(new BigDecimal("49.90"));
        when(product.getImageUrl()).thenReturn("https://example.com/image.jpg");
        when(product.getPurchaseUrl()).thenReturn("https://example.com/buy");
        when(product.getStatus()).thenReturn(ProductStatus.PUBLISHED);
        when(product.getSizes()).thenReturn(List.of("M"));
        when(product.getMaterials()).thenReturn(List.of("algodao"));
        when(product.getGender()).thenReturn(com.ages.pie.domain.enums.ProductGender.FEMININO);
        when(productRepository.findByActiveTrueAndStatus(ProductStatus.PUBLISHED)).thenReturn(List.of(product));

        var result = service.recommendCatalogForUser(userId, PageRequest.of(0, 20));

        assertThat(result.items()).hasSize(1);
        var item = result.items().getFirst();
        assertThat(item.id()).isEqualTo(product.getId());
        assertThat(item.name()).isEqualTo("Camiseta");
        assertThat(item.category()).isEqualTo("camiseta");
        assertThat(item.color()).isEqualTo("azul");
        assertThat(item.price()).isEqualByComparingTo("49.90");
        assertThat(item.imageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(item.purchaseUrl()).isEqualTo("https://example.com/buy");
        assertThat(item.companyName()).isEqualTo("Loja parceira");
        assertThat(item.status()).isEqualTo(ProductStatus.PUBLISHED);
        assertThat(item.styles()).containsExactly("casual");
        assertThat(item.sizes()).containsExactly("M");
        assertThat(item.materials()).containsExactly("algodao");
        assertThat(item.gender()).isEqualTo(com.ages.pie.domain.enums.ProductGender.FEMININO);
    }
}
