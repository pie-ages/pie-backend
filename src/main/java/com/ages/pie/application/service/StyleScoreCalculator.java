package com.ages.pie.application.service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.ages.pie.domain.enums.Style;

public final class StyleScoreCalculator {
    private static final List<Style> CATALOG = List.of(Style.values());

    private StyleScoreCalculator() {
    }

    private static Map<Style, Integer> scores(List<Style> answers) {
        Map<Style, Integer> scores = new EnumMap<>(Style.class);
        if (answers != null) {
            answers.forEach(style -> scores.merge(style, 1, Integer::sum));
        }
        return scores;
    }

    /** Pontos positivos em ordem decrescente; empates seguem a ordem fixa do catálogo. */
    public static List<Style> rankStyles(List<Style> answers) {
        Map<Style, Integer> scores = scores(answers);
        return CATALOG.stream().filter(scores::containsKey)
                .sorted(Comparator.comparingInt((Style style) -> scores.get(style)).reversed())
                .toList();
    }

    public static List<Style> calculateTopStyles(List<Style> answers) {
        List<Style> ranked = rankStyles(answers);
        if (ranked.isEmpty()) {
            throw new IllegalArgumentException("Respostas são obrigatórias para calcular o estilo");
        }
        Map<Style, Integer> scores = scores(answers);
        int best = scores.get(ranked.getFirst());
        return ranked.stream().filter(style -> scores.get(style) == best).toList();
    }
}
