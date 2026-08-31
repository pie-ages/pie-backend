package com.ages.pie.application.dto;

public record LoginResponseDTO(
    String token,
    UserSummaryDTO user
) {
}
