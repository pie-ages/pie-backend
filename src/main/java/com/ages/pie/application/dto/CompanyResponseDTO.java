package com.ages.pie.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CompanyResponseDTO(
    UUID id,
    String name,
    String cnpj,
    String socialReason,
    String responsiblePerson,
    String email,
    String website,
    boolean active,
    String photoUrl,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
