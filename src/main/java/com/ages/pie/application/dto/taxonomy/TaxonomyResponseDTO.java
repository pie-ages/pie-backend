package com.ages.pie.application.dto.taxonomy;

import java.util.List;

public record TaxonomyResponseDTO(
    List<TaxonomyTermDTO> categories,
    List<TaxonomyTermDTO> colors,
    List<TaxonomyTermDTO> styles,
    List<TaxonomyTermDTO> sizes,
    List<TaxonomyTermDTO> materials
) {
}
