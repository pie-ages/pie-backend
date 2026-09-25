package com.ages.pie.application.dto.look;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AddLookWardrobeItemRequestDTO(
    @NotNull(message = "Peça do guarda-roupa é obrigatória")
    UUID wardrobeItemId
) {
}
