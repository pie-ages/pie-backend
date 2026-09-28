package com.ages.pie.application.dto.look;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record LookResponseDTO(
    UUID id,
    String title,
    String description,
    String occasion,
    String photoUrl,
    boolean aiGenerated,
    List<LookItemDTO> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
