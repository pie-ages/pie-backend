package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.product.ProductCatalogItemDTO;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.infrastructure.repository.ProductImageRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ProductMapperTest {

    private final ProductMapper productMapper = new ProductMapper(mock(ProductImageRepository.class));

    private Product productOf(String photoUrl) {
        Company company = new Company(UUID.randomUUID(), "Loja X", "12345678000199", "Loja X LTDA",
                "Maria", "contato@lojax.com", UUID.randomUUID(), null, photoUrl);
        return new Product(company, "Blazer Social Feminino");
    }

    @Test
    void toCatalogItemDTO_shouldExposeCompanyPhotoUrl() {
        ProductCatalogItemDTO dto = productMapper.toCatalogItemDTO(productOf("https://storage/logo.png"));

        assertThat(dto.companyName()).isEqualTo("Loja X");
        assertThat(dto.companyPhotoUrl()).isEqualTo("https://storage/logo.png");
    }

    @Test
    void toCatalogItemDTO_shouldReturnNullCompanyPhotoUrl_whenCompanyHasNoLogo() {
        ProductCatalogItemDTO dto = productMapper.toCatalogItemDTO(productOf(null));

        assertThat(dto.companyPhotoUrl()).isNull();
    }
}
