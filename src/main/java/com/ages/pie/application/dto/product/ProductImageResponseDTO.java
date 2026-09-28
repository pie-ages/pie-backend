package com.ages.pie.application.dto.product;

import java.util.UUID;

public record ProductImageResponseDTO(
    UUID id,
    String url,
    boolean isPrimary,
    int displayOrder
) {}
