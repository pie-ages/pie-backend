package com.ages.pie.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequestDTO(
    @NotBlank String refreshToken
) {
}
