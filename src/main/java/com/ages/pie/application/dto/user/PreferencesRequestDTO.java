package com.ages.pie.application.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record PreferencesRequestDTO(
        @NotNull(message = "A lista de cores favoritas é obrigatória")
        List<@Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor inválida. Use formato hexadecimal: #RRGGBB")
             String> favoriteColors
) {
}
