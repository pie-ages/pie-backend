package com.ages.pie.application.dto.user;

import java.util.List;

import com.ages.pie.domain.enums.ProductStyle;

public record UserStyleResponseDTO(
    List<ProductStyle> styles
) {
}
