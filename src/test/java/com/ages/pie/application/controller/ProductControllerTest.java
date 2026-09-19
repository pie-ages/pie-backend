package com.ages.pie.application.controller;

import com.ages.pie.application.dto.product.ProductCatalogItemDTO;
import com.ages.pie.application.dto.product.ProductCatalogPageDTO;
import com.ages.pie.application.dto.product.ProductResponseDTO;
import com.ages.pie.application.service.ProductService;
import com.ages.pie.domain.enums.ProductStatus;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private AuthenticatedUserProvider authenticatedUserProvider;

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID COMPANY_ID = UUID.randomUUID();

    private ProductResponseDTO sampleResponse() {
        return new ProductResponseDTO(
                PRODUCT_ID, "Camiseta", "100% algodão", "Camiseta",
                "Branco", new BigDecimal("49.90"), "https://img.com/camiseta.jpg",
                "https://loja.com/camiseta", ProductStatus.RASCUNHO, "Loja X",
                OffsetDateTime.now());
    }

    // ── POST /products ────────────────────────────────────────────────────

    @Test
    void create_shouldReturn201WithProduct() throws Exception {
        when(productService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Camiseta",
                                    "category": "Camiseta",
                                    "color": "Branco",
                                    "price": 49.90,
                                    "companyId": "%s"
                                }
                                """.formatted(COMPANY_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Camiseta"))
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.color").value("Branco"));
    }

    @Test
    void create_shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "price": 49.90,
                                    "companyId": "%s"
                                }
                                """.formatted(COMPANY_ID)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenPriceIsMissing() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Camiseta",
                                    "companyId": "%s"
                                }
                                """.formatted(COMPANY_ID)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400WhenCompanyIdIsNull() throws Exception {
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Camiseta",
                                    "price": 49.90
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── GET /products ─────────────────────────────────────────────────────

    @Test
    void findCatalog_shouldReturnPaginatedProducts() throws Exception {
        ProductCatalogItemDTO item = new ProductCatalogItemDTO(
                PRODUCT_ID, "Camiseta", "Camiseta", "Branco", new BigDecimal("49.90"),
                "https://img.com/camiseta.jpg", "https://loja.com/camiseta",
                "Loja X", ProductStatus.PUBLICADO);
        ProductCatalogPageDTO page = new ProductCatalogPageDTO(List.of(item), 1, 0, 20);
        when(productService.findCatalog(any(), any())).thenReturn(page);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].name").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].status").value("PUBLICADO"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void findCatalog_shouldPassSearchParam() throws Exception {
        ProductCatalogPageDTO empty = new ProductCatalogPageDTO(List.of(), 0, 0, 20);
        when(productService.findCatalog(eq("blazer"), any())).thenReturn(empty);

        mockMvc.perform(get("/products").param("search", "blazer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ── GET /products/{id} ────────────────────────────────────────────────

    @Test
    void findById_shouldReturnProduct() throws Exception {
        when(productService.findById(PRODUCT_ID)).thenReturn(sampleResponse());

        mockMvc.perform(get("/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.status").value("RASCUNHO"));
    }

    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(productService.findById(PRODUCT_ID))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Produto não encontrado"));

        mockMvc.perform(get("/products/{id}", PRODUCT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Produto não encontrado"));
    }

    // ── PUT /products/{id} ────────────────────────────────────────────────

    @Test
    void update_shouldReturnUpdatedProduct() throws Exception {
        when(productService.update(eq(PRODUCT_ID), any())).thenReturn(sampleResponse());

        mockMvc.perform(put("/products/{id}", PRODUCT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Camiseta Premium",
                                    "price": 59.90
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()));
    }

    // ── PATCH /products/{id}/deactivate ───────────────────────────────────

    @Test
    void deactivate_shouldReturn204() throws Exception {
        mockMvc.perform(patch("/products/{id}/deactivate", PRODUCT_ID))
                .andExpect(status().isNoContent());
    }

    // ── GET /products/company/{companyId} ─────────────────────────────────

    @Test
    void findByCompany_shouldReturnFilteredProducts() throws Exception {
        ProductCatalogItemDTO item = new ProductCatalogItemDTO(
                PRODUCT_ID, "Camiseta", "Camiseta", "Branco", new BigDecimal("49.90"),
                "https://img.com/camiseta.jpg", "https://loja.com/camiseta",
                "Loja X", ProductStatus.PUBLICADO);
        ProductCatalogPageDTO page = new ProductCatalogPageDTO(List.of(item), 1, 0, 20);
        when(productService.findByCompany(eq(COMPANY_ID), eq(ProductStatus.PUBLICADO), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/products/company/{companyId}", COMPANY_ID)
                        .param("status", "PUBLICADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].category").value("Camiseta"))
                .andExpect(jsonPath("$.items[0].color").value("Branco"))
                .andExpect(jsonPath("$.items[0].status").value("PUBLICADO"));
    }

    @Test
    void findByCompany_shouldReturnAllProducts_whenStatusIsAbsent() throws Exception {
        ProductCatalogPageDTO page = new ProductCatalogPageDTO(List.of(), 0, 0, 20);
        when(productService.findByCompany(eq(COMPANY_ID), eq(null), any(), any())).thenReturn(page);

        mockMvc.perform(get("/products/company/{companyId}", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void findByCompany_shouldReturn404WhenCompanyNotFound() throws Exception {
        when(productService.findByCompany(eq(COMPANY_ID), any(), any(), any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Empresa não encontrada"));

        mockMvc.perform(get("/products/company/{companyId}", COMPANY_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Empresa não encontrada"));
    }

    // ── PATCH /products/{id}/publish ──────────────────────────────────────

    @Test
    void publish_shouldReturn200WithUpdatedProduct() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        ProductResponseDTO published = new ProductResponseDTO(
                PRODUCT_ID, "Camiseta", null, "Camiseta", "Branco", new BigDecimal("49.90"),
                null, null, ProductStatus.PUBLICADO, "Loja X", OffsetDateTime.now());
        when(productService.publish(PRODUCT_ID, COMPANY_ID)).thenReturn(published);

        mockMvc.perform(patch("/products/{id}/publish", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLICADO"));
    }

    @Test
    void publish_shouldReturn409WhenAlreadyPublished() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        when(productService.publish(PRODUCT_ID, COMPANY_ID))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Produto já está publicado"));

        mockMvc.perform(patch("/products/{id}/publish", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Produto já está publicado"));
    }

    @Test
    void publish_shouldReturn403WhenNotOwner() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        when(productService.publish(PRODUCT_ID, COMPANY_ID))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "Apenas a empresa dona do produto pode alterar a disponibilidade"));

        mockMvc.perform(patch("/products/{id}/publish", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /products/{id}/unpublish ────────────────────────────────────

    @Test
    void unpublish_shouldReturn200WithUpdatedProduct() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        ProductResponseDTO paused = new ProductResponseDTO(
                PRODUCT_ID, "Camiseta", null, "Camiseta", "Branco", new BigDecimal("49.90"),
                null, null, ProductStatus.PAUSADO, "Loja X", OffsetDateTime.now());
        when(productService.unpublish(PRODUCT_ID, COMPANY_ID)).thenReturn(paused);

        mockMvc.perform(patch("/products/{id}/unpublish", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSADO"));
    }

    @Test
    void unpublish_shouldReturn409WhenNotPublished() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        when(productService.unpublish(PRODUCT_ID, COMPANY_ID))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Produto não está publicado"));

        mockMvc.perform(patch("/products/{id}/unpublish", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Produto não está publicado"));
    }

    // ── DELETE /products/{id} ─────────────────────────────────────────────

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/products/{id}", PRODUCT_ID))
                .andExpect(status().isNoContent());
    }
}
