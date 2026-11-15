package com.ages.pie.infrastructure.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.service.RecommendationScorer;

/**
 * Adapter da Port {@link RecommendationScorer} que pontua por combinação de
 * estilos e cores — o critério da v1, sem depender de serviço externo.
 *
 * Para trocar por outro critério (um modelo de IA, por exemplo), basta criar
 * outra implementação da interface; nada em {@code RecommendationService} muda.
 */
@Component
public class HeuristicRecommendationScorer implements RecommendationScorer {

    public static final double STYLE_WEIGHT = 50.0;
    public static final double COLOR_WEIGHT = 50.0;

    @Override
    public double score(Set<String> userStyleIds, Set<String> favoriteColors, Product product) {
        return styleScore(userStyleIds, product) + colorScore(favoriteColors, product);
    }

    private double styleScore(Set<String> userStyleIds, Product product) {
        List<String> productStyles = product.getStyles();
        if (userStyleIds.isEmpty() || productStyles == null || productStyles.isEmpty()) {
            return 0.0;
        }
        Set<String> normalized = productStyles.stream()
                .map(HeuristicRecommendationScorer::normalize)
                .collect(Collectors.toSet());
        long matches = normalized.stream().filter(userStyleIds::contains).count();
        return STYLE_WEIGHT * ((double) matches / normalized.size());
    }

    private double colorScore(Set<String> favoriteColors, Product product) {
        if (favoriteColors.isEmpty() || product.getColor() == null) {
            return 0.0;
        }
        return favoriteColors.contains(normalize(product.getColor())) ? COLOR_WEIGHT : 0.0;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
