package com.ages.pie.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequestDTO(
    @NotBlank(message = "Nome é obrigatório")
    String name,

    String description,

    String category,

    @NotNull(message = "Preço é obrigatório")
    @Positive(message = "Preço deve ser maior que zero")
    BigDecimal price,

    String imageUrl,

    String purchaseUrl,

    @NotNull(message = "Empresa é obrigatória")
    UUID companyId
) {
}
