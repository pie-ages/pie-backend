package com.ages.pie.application.dto.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDTO(
    UUID id,
    String name,
    String email,
    String photoUrl,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
