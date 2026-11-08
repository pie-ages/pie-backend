package com.ages.pie.application.dto.user;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.ages.pie.domain.enums.AccountSex;

public record UserResponseDTO(
    UUID id,
    String name,
    String email,
    String photoUrl,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    AccountSex sex
) {
}