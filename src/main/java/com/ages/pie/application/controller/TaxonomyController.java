package com.ages.pie.application.controller;

import com.ages.pie.application.dto.taxonomy.TaxonomyResponseDTO;
import com.ages.pie.application.service.TaxonomyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/taxonomy")
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    public TaxonomyController(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    @GetMapping
    public ResponseEntity<TaxonomyResponseDTO> getTaxonomy() {
        return ResponseEntity.ok(taxonomyService.getTaxonomy());
    }
}
