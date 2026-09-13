package com.ages.pie.application.dto.product;

import java.util.List;

public record ProductCatalogPageDTO(
    List<ProductCatalogItemDTO> items,
    long total,
    int page,
    int size
) {
}
