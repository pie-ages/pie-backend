package com.ages.pie.application.dto.style;

import java.util.UUID;

public record StyleOptionResponseDTO(
    UUID id,
    String label,
    String imageUrl,
    int displayOrder
) {
}
