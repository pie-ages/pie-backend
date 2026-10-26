package com.ages.pie.application.dto.look;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LookRequestDTO(
    @NotBlank(message = "Título é obrigatório")
    String title,

    String description,

    String occasion,

    List<@NotNull(message = "Peça do guarda-roupa é obrigatória") UUID> wardrobeItemIds,

    List<@NotNull(message = "Produto é obrigatório") UUID> productIds,

    /** Peças na ordem definida pelo usuário. Quando informado, prevalece sobre wardrobeItemIds/productIds. */
    List<@NotNull(message = "Peça é obrigatória") @Valid LookItemRefDTO> items
) {
    public LookRequestDTO(String title, String description, String occasion,
            List<UUID> wardrobeItemIds, List<UUID> productIds) {
        this(title, description, occasion, wardrobeItemIds, productIds, null);
    }
}
