package com.ages.pie.application.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCatalogItemDTO(
    UUID id,
    String name,
    BigDecimal price,
    String imageUrl,
    String purchaseUrl,
    String companyName
) {
}
