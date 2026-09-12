package com.ages.pie.application.service;

import com.ages.pie.application.dto.product.ProductCatalogItemDTO;
import com.ages.pie.application.dto.product.ProductCatalogPageDTO;
import com.ages.pie.application.mapper.ProductMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.entity.Product;
import com.ages.pie.infrastructure.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Pageable pageable;
    private Product product;
    private ProductCatalogPageDTO pageDTO;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 20);

        Company company = new Company(UUID.randomUUID(), "Loja X", "12345678000199", "Loja X LTDA",
                "Maria", "contato@lojax.com", "hash(senha123)", null, null);
        product = new Product(company, "Blazer Social Feminino");

        ProductCatalogItemDTO itemDTO = new ProductCatalogItemDTO(UUID.randomUUID(), "Blazer Social Feminino",
                BigDecimal.valueOf(279.90), null, null, "Loja X");
        pageDTO = new ProductCatalogPageDTO(List.of(itemDTO), 1, 0, 20);
    }

    @Test
    void findCatalog_shouldPassNullSearch_whenSearchIsNull() {
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findCatalog(isNull(), eq(pageable))).thenReturn(page);
        when(productMapper.toCatalogPageDTO(page)).thenReturn(pageDTO);

        ProductCatalogPageDTO result = productService.findCatalog(null, pageable);

        assertThat(result).isEqualTo(pageDTO);
        verify(productRepository).findCatalog(isNull(), eq(pageable));
    }

    @Test
    void findCatalog_shouldPassNullSearch_whenSearchIsBlank() {
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findCatalog(isNull(), eq(pageable))).thenReturn(page);
        when(productMapper.toCatalogPageDTO(page)).thenReturn(pageDTO);

        productService.findCatalog("   ", pageable);

        verify(productRepository).findCatalog(isNull(), eq(pageable));
    }

    @Test
    void findCatalog_shouldTrimSearch_whenSearchHasSurroundingWhitespace() {
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);
        ArgumentCaptor<String> searchCaptor = ArgumentCaptor.forClass(String.class);
        when(productRepository.findCatalog(searchCaptor.capture(), eq(pageable))).thenReturn(page);
        when(productMapper.toCatalogPageDTO(page)).thenReturn(pageDTO);

        productService.findCatalog("  blazer  ", pageable);

        assertThat(searchCaptor.getValue()).isEqualTo("blazer");
    }

    @Test
    void findCatalog_shouldReturnEmptyPage_whenNoResults() {
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        ProductCatalogPageDTO emptyDTO = new ProductCatalogPageDTO(List.of(), 0, 0, 20);
        when(productRepository.findCatalog(any(), eq(pageable))).thenReturn(emptyPage);
        when(productMapper.toCatalogPageDTO(emptyPage)).thenReturn(emptyDTO);

        ProductCatalogPageDTO result = productService.findCatalog("inexistente", pageable);

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
    }
}
