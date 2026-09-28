package com.ages.pie.application.dto.look;

import jakarta.validation.constraints.NotBlank;

public record LookUpdateDTO(
    @NotBlank(message = "Título é obrigatório")
    String title,

    String description,

    String occasion
) {
}
