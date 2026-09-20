package com.ages.pie.application.dto.product;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductUpdateDTO(
    String name,

    String description,

    String category,

    String color,

    List<String> styles,

    List<String> materials,

    List<String> sizes,

    @Positive(message = "Preço deve ser maior que zero")
    BigDecimal price,

    String imageUrl,

    String purchaseUrl,

    UUID companyId
) {
}
