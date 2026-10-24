package com.ages.pie.application.dto.user;

import com.ages.pie.domain.enums.ColorimetrySeason;

public record ColorimetrySeasonDTO(
        String id,
        String label,
        String highlightCaption,
        String avoidCaption
) {
    public static ColorimetrySeasonDTO from(ColorimetrySeason season) {
        return new ColorimetrySeasonDTO(
                season.name().toLowerCase(),
                season.getLabel(),
                season.getHighlightCaption(),
                season.getAvoidCaption()
        );
    }
}
