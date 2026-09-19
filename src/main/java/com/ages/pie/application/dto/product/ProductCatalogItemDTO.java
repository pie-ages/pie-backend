package com.ages.pie.application.dto.product;

import com.ages.pie.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductCatalogItemDTO(
    UUID id,
    String name,
    String category,
    String color,
    BigDecimal price,
    String imageUrl,
    String purchaseUrl,
    String companyName,
    ProductStatus status,
    String style,
    List<String> sizes
) {
}
