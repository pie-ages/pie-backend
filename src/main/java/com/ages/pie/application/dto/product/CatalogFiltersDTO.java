package com.ages.pie.application.dto.product;

import java.util.List;
import java.util.UUID;

public record CatalogFiltersDTO(
    List<String> styles,
    List<String> categories,
    List<String> colors,
    List<UUID> companies,
    List<String> materials
) {}
