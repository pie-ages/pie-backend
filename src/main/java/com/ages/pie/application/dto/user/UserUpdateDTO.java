package com.ages.pie.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import com.ages.pie.domain.enums.AccountSex;

public record UserUpdateDTO(
    @NotBlank(message = "Nome é obrigatório")
    String name,

    String photoUrl,

    AccountSex sex
) {
}