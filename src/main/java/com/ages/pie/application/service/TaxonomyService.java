package com.ages.pie.application.service;

import com.ages.pie.application.dto.taxonomy.TaxonomyResponseDTO;
import com.ages.pie.application.dto.taxonomy.TaxonomyTermDTO;
import com.ages.pie.domain.enums.ProductCategory;
import com.ages.pie.domain.enums.ProductColor;
import com.ages.pie.domain.enums.ProductMaterial;
import com.ages.pie.domain.enums.ProductSize;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.domain.enums.TaxonomyItem;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class TaxonomyService {

    public TaxonomyResponseDTO getTaxonomy() {
        return new TaxonomyResponseDTO(
            toTermList(ProductCategory.values()),
            toTermList(ProductColor.values()),
            toTermList(ProductStyle.values()),
            toTermList(ProductSize.values()),
            toTermList(ProductMaterial.values())
        );
    }

    private List<TaxonomyTermDTO> toTermList(TaxonomyItem[] items) {
        return Arrays.stream(items)
            .map(i -> new TaxonomyTermDTO(i.getId(), i.getName()))
            .toList();
    }
}
