package com.ages.pie.application.dto.wishlist;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WishlistItemResponseDTO(
    UUID id,
    UUID productId,
    String name,
    String color,
    BigDecimal price,
    String imageUrl,
    String purchaseUrl,
    boolean available,
    OffsetDateTime addedAt
) {
}
