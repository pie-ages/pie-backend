package com.ages.pie.application.dto.user;

import java.util.List;

public record PreferencesResponseDTO(
        List<String> highlightColors,
        List<String> avoidColors,
        List<String> favoriteColors
) {
}
