package com.ages.pie.application.dto.product;

import java.util.List;

/**
 * @param ignoredFilters filtros de perfil ("colorimetry", "matchesProfile") que foram solicitados
 *                       mas não aplicados por falta de dados do usuário.
 */
public record ProductCatalogPageDTO(
    List<ProductCatalogItemDTO> items,
    long total,
    int page,
    int size,
    List<String> ignoredFilters
) {
    public ProductCatalogPageDTO(List<ProductCatalogItemDTO> items, long total, int page, int size) {
        this(items, total, page, size, List.of());
    }

    public ProductCatalogPageDTO withIgnoredFilters(List<String> ignored) {
        return new ProductCatalogPageDTO(items, total, page, size, ignored);
    }
}
