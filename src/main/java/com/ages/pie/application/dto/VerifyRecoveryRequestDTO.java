package com.ages.pie.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyRecoveryRequestDTO(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @NotBlank(message = "Código é obrigatório")
    @Pattern(regexp = "\\d{6}", message = "O código deve ter 6 dígitos")
    String code
) {
}
