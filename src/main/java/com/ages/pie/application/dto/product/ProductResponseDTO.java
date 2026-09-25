package com.ages.pie.application.dto.product;

import com.ages.pie.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponseDTO(
    UUID id,
    String name,
    String description,
    String category,
    String color,
    BigDecimal price,
    String imageUrl,
    String purchaseUrl,
    ProductStatus status,
    String companyName,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<String> styles,
    List<String> sizes,
    List<String> materials
) {
}
