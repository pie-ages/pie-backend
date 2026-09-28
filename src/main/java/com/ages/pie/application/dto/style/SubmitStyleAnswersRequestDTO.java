package com.ages.pie.application.dto.style;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record SubmitStyleAnswersRequestDTO(
    @NotEmpty(message = "Respostas são obrigatórias")
    @Valid
    List<StyleAnswerRequestDTO> answers
) {
}
