package com.ages.pie.application.dto.wardrobe;

import java.util.UUID;

public record WardrobeItemResponseDTO(
        UUID id,
        UUID productId,
        String category,
        String color,
        String photoUrl
) {
}