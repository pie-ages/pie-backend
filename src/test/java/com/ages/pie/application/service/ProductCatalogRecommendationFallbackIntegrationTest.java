package com.ages.pie.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.dto.product.CatalogFiltersDTO;
import com.ages.pie.application.dto.product.ProductCatalogPageDTO;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.enums.ProductStatus;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import com.ages.pie.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class ProductCatalogRecommendationFallbackIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void findCatalog_shouldFallbackToCatalog_whenAuthenticatedUserHasNoRecommendationProfile() {
        Company company = companyRepository.save(new Company(null, "Loja", unique14(), "Loja LTDA", "Maria",
                "loja-" + UUID.randomUUID() + "@teste.com", UUID.randomUUID(), null, null));
        Product product = new Product(company, "Camiseta fallback");
        product.setStatus(ProductStatus.PUBLISHED);
        product.setActive(true);
        productRepository.saveAndFlush(product);

        CatalogFiltersDTO emptyFilters = new CatalogFiltersDTO(null, null, null, null, null);
        UUID missingUserId = UUID.randomUUID();

        ProductCatalogPageDTO result = productService.findCatalog(null, emptyFilters, PageRequest.of(0, 20),
                Optional.of(missingUserId));

        assertThat(result).isNotNull();
        assertThat(result.items()).isNotEmpty();
        assertThat(result.page()).isZero();
    }

    private static String unique14() {
        return String.format("%014d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100_000_000_000_000L);
    }
}
