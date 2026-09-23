package com.ages.pie.application.dto.user;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record UserStyleUpdateDTO(
    @NotNull(message = "Estilos são obrigatórios")
    List<String> styles
) {
}
