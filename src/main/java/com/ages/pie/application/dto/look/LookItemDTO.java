package com.ages.pie.application.dto.look;

import java.util.UUID;

public record LookItemDTO(
    UUID wardrobeItemId,
    UUID productId,
    String name,
    String category,
    String color,
    String imageUrl
) {
}
