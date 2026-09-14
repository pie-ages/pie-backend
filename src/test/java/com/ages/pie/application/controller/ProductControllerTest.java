package com.ages.pie.application.controller;

import com.ages.pie.application.dto.product.ProductCatalogItemDTO;
import com.ages.pie.application.dto.product.ProductCatalogPageDTO;
import com.ages.pie.application.dto.product.ProductResponseDTO;
import com.ages.pie.application.service.ProductService;
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
                PRODUCT_ID, "Camiseta", "100% algodão", "Roupas",
                new BigDecimal("49.90"), "https://img.com/camiseta.jpg",
                "https://loja.com/camiseta", true, true, "Loja X",
                OffsetDateTime.now());
    }


    @Test
    void create_shouldReturn201WithProduct() throws Exception {
        when(productService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Camiseta",
                                    "description": "100%% algodão",
                                    "category": "Roupas",
                                    "price": 49.90,
                                    "imageUrl": "https://img.com/camiseta.jpg",
                                    "purchaseUrl": "https://loja.com/camiseta",
                                    "companyId": "%s"
                                }
                                """.formatted(COMPANY_ID.toString())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Camiseta"))
                .andExpect(jsonPath("$.price").value(49.90))
                .andExpect(jsonPath("$.available").value(true));
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
                                """.formatted(COMPANY_ID.toString())))
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
                                """.formatted(COMPANY_ID.toString())))
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


    @Test
    void findCatalog_shouldReturnPaginatedProducts() throws Exception {
        ProductCatalogItemDTO item = new ProductCatalogItemDTO(
                PRODUCT_ID, "Camiseta", new BigDecimal("49.90"),
                "https://img.com/camiseta.jpg", "https://loja.com/camiseta",
                "Loja X", true);
        ProductCatalogPageDTO page = new ProductCatalogPageDTO(List.of(item), 1, 0, 20);
        when(productService.findCatalog(any(), any())).thenReturn(page);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].name").value("Camiseta"))
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


    @Test
    void findById_shouldReturnProduct() throws Exception {
        when(productService.findById(PRODUCT_ID)).thenReturn(sampleResponse());

        mockMvc.perform(get("/products/{id}", PRODUCT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Camiseta"));
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


    @Test
    void deactivate_shouldReturn204() throws Exception {
        mockMvc.perform(patch("/products/{id}/deactivate", PRODUCT_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void publish_shouldReturn200WithUpdatedProduct() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        when(productService.publish(PRODUCT_ID, COMPANY_ID)).thenReturn(sampleResponse());

        mockMvc.perform(patch("/products/{id}/publish", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()));
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


    @Test
    void unpublish_shouldReturn200WithUpdatedProduct() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        when(productService.unpublish(PRODUCT_ID, COMPANY_ID)).thenReturn(sampleResponse());

        mockMvc.perform(patch("/products/{id}/unpublish", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()));
    }

    @Test
    void unpublish_shouldReturn409WhenAlreadyUnpublished() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        when(productService.unpublish(PRODUCT_ID, COMPANY_ID))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Produto já está despublicado"));

        mockMvc.perform(patch("/products/{id}/unpublish", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isConflict());
    }


    @Test
    void available_shouldDelegateToPublish() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        when(productService.publish(PRODUCT_ID, COMPANY_ID)).thenReturn(sampleResponse());

        mockMvc.perform(patch("/products/{id}/available", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()));
    }

    // ── PATCH /products/{id}/unavailable (alias) ─────────────────────────

    @Test
    void unavailable_shouldDelegateToUnpublish() throws Exception {
        when(authenticatedUserProvider.id()).thenReturn(COMPANY_ID);
        when(productService.unpublish(PRODUCT_ID, COMPANY_ID)).thenReturn(sampleResponse());

        mockMvc.perform(patch("/products/{id}/unavailable", PRODUCT_ID)
                        .header("X-User-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()));
    }


    @Test
    void findByCompany_shouldReturnFilteredProducts() throws Exception {
        ProductCatalogItemDTO item = new ProductCatalogItemDTO(
                PRODUCT_ID, "Camiseta", new BigDecimal("49.90"),
                "https://img.com/camiseta.jpg", "https://loja.com/camiseta",
                "Loja X", true);
        ProductCatalogPageDTO page = new ProductCatalogPageDTO(List.of(item), 1, 0, 20);
        when(productService.findByCompany(eq(COMPANY_ID), eq(true), any(), any())).thenReturn(page);

        mockMvc.perform(get("/products/company/{companyId}", COMPANY_ID)
                        .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].name").value("Camiseta"));
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

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/products/{id}", PRODUCT_ID))
                .andExpect(status().isNoContent());
    }
}
