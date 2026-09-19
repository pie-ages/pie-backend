package com.ages.pie.application.dto.product;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpdateDTO(
    String name,

    String description,

    String category,

    String color,

    @Positive(message = "Preço deve ser maior que zero")
    BigDecimal price,

    String imageUrl,

    String purchaseUrl,

    UUID companyId
) {
}
