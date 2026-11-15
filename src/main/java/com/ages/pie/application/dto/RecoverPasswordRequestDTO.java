package com.ages.pie.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecoverPasswordRequestDTO(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email
) {
}
