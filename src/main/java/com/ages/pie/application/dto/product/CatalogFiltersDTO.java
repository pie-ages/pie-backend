package com.ages.pie.application.dto.product;

import java.util.List;
import java.util.UUID;

public record CatalogFiltersDTO(
    List<String> styles,
    List<String> categories,
    List<String> colors,
    List<UUID> companies,
    List<String> materials,
    List<String> genders,
    boolean colorimetry,
    boolean matchesProfile
) {
    public CatalogFiltersDTO(List<String> styles, List<String> categories, List<String> colors,
            List<UUID> companies, List<String> materials) {
        this(styles, categories, colors, companies, materials, null, false, false);
    }
}
