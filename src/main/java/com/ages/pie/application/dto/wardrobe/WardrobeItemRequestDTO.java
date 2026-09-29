package com.ages.pie.application.dto.wardrobe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record WardrobeItemRequestDTO(
        UUID productId,

        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank(message = "Categoria é obrigatória")
        String category,

        @Pattern(regexp = "casual|classic|sport|party", message = "Estilo inválido")
        String style,

        String color
) {
}