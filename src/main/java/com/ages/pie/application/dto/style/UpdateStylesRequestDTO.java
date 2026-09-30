package com.ages.pie.application.dto.style;

import java.util.List;

import com.ages.pie.domain.enums.Style;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateStylesRequestDTO(
        @NotEmpty List<@NotNull Style> styles
) {
}
