package com.ages.pie.application.dto;

public record LoginResponseDTO(
    String accessToken,
    String refreshToken,
    long expiresIn,
    String tokenType,
    UserSummaryDTO user
) {
}
