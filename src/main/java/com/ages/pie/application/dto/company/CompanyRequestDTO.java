package com.ages.pie.application.dto.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompanyRequestDTO(
    String name,
    String cnpj,
    String socialReason,
    String responsiblePerson,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    String password,
    String website,
    String photoUrl
) {
}
