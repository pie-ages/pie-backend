package com.ages.pie.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductResponseDTO(
    UUID id,
    String name,
    String description,
    String category,
    BigDecimal price,
    String imageUrl,
    String purchaseUrl,
    boolean active,
    String companyName,
    OffsetDateTime createdAt
) {
}
