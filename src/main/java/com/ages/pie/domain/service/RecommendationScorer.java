package com.ages.pie.domain.service;

import java.util.Set;

import com.ages.pie.domain.entity.Product;

public interface RecommendationScorer {

    double score(Set<String> userStyleIds, Set<String> favoriteColors, Product product);
}
