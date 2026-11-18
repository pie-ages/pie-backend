package com.ages.pie.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequestDTO(
    @NotBlank(message = "Senha é obrigatória")
    String password
) {
}
