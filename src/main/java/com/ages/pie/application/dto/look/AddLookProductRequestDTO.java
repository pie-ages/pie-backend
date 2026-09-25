package com.ages.pie.application.dto.look;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AddLookProductRequestDTO(
    @NotNull(message = "Produto é obrigatório")
    UUID productId
) {
}
