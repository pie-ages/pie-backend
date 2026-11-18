package com.ages.pie.application.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.ages.pie.domain.enums.AccountSex;

public record UserRequestDTO(
    @NotBlank(message = "Nome é obrigatório")
    String name,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    String password,

    AccountSex sex
) {
}