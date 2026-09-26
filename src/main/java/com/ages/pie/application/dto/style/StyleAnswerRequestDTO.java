package com.ages.pie.application.dto.style;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record StyleAnswerRequestDTO(
    @NotNull(message = "Pergunta é obrigatória")
    UUID questionId,

    @NotNull(message = "Opção é obrigatória")
    UUID optionId
) {
}
