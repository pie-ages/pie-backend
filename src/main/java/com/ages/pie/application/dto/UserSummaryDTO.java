package com.ages.pie.application.dto;

import java.util.UUID;

public record UserSummaryDTO(
    UUID id,
    String name,
    String email,
    String photoUrl
) {
}
