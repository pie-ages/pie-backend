package com.ages.pie.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RecommendedProductDTO(
        UUID id,
        String name,
        String category,
        String color,
        BigDecimal price,
        String imageUrl,
        double score) {
}
