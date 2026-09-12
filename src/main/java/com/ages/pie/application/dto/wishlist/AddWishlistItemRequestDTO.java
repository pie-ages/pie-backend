package com.ages.pie.application.dto.wishlist;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AddWishlistItemRequestDTO(
    @NotNull(message = "Produto é obrigatório")
    UUID productId
) {
}
