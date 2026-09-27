package com.ages.pie.application.dto.style;

import java.util.UUID;

import com.ages.pie.domain.enums.StyleAnswerType;
import jakarta.validation.constraints.NotNull;

public record StyleAnswerRequestDTO(
    @NotNull(message = "Pergunta é obrigatória")
    UUID questionId,

    UUID optionId,

    @NotNull(message = "Tipo de resposta é obrigatório")
    StyleAnswerType answerType
) {
}
