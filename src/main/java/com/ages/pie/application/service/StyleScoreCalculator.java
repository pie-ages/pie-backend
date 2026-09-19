package com.ages.pie.application.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ages.pie.domain.enums.Style;

public final class StyleScoreCalculator {

    private StyleScoreCalculator() {
    }

    public static Style calculate(List<Style> stylesInAnswerOrder) {
        if (stylesInAnswerOrder == null || stylesInAnswerOrder.isEmpty()) {
            throw new IllegalArgumentException("Respostas são obrigatórias para calcular o estilo");
        }

        Map<Style, Integer> votes = new LinkedHashMap<>();
        for (Style style : stylesInAnswerOrder) {
            votes.merge(style, 1, Integer::sum);
        }

        Style winner = null;
        int best = -1;
        for (Map.Entry<Style, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > best) {
                best = entry.getValue();
                winner = entry.getKey();
            }
        }
        return winner;
    }
}
