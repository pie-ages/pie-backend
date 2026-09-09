package com.ages.pie.application.dto.wishlist;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record WishlistResponseDTO(
    UUID id,
    String name,
    List<WishlistItemResponseDTO> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
