package com.ages.pie.application.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(
    @NotBlank(message = "Nome é obrigatório")
    String name,

    String photoUrl
) {
}
