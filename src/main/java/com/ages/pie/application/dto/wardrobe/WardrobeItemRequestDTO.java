package com.ages.pie.application.dto.wardrobe;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record WardrobeItemRequestDTO(
        UUID productId,

        @NotBlank(message = "Categoria é obrigatória")
        String category,

        String color
) {
}