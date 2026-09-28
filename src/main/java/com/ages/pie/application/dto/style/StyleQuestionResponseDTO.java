package com.ages.pie.application.dto.style;

import java.util.List;
import java.util.UUID;

public record StyleQuestionResponseDTO(
    UUID id,
    String question,
    int order,
    List<StyleOptionResponseDTO> options
) {
}
