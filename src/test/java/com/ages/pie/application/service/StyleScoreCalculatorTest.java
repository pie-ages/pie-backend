package com.ages.pie.application.service;

import com.ages.pie.domain.enums.Style;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class StyleScoreCalculatorTest {
    @ParameterizedTest
    @EnumSource(Style.class)
    void everyCatalogStyleCanBeTheEssence(Style style) {
        assertThat(StyleScoreCalculator.rankStyles(List.of(Style.CASUAL, style, style)).getFirst()).isEqualTo(style);
    }

    @Test
    void ordersPositiveVotesByScoreAndBreaksTiesConsistently() {
        var votes = List.of(Style.ROMANTICO, Style.ELEGANTE, Style.BOHO, Style.ELEGANTE, Style.ROMANTICO);
        assertThat(StyleScoreCalculator.rankStyles(votes)).containsExactly(Style.ELEGANTE, Style.ROMANTICO, Style.BOHO);
        assertThat(StyleScoreCalculator.calculateTopStyles(votes)).containsExactly(Style.ELEGANTE, Style.ROMANTICO);
    }

    @Test
    void noVotesDoesNotInventStyles() {
        assertThat(StyleScoreCalculator.rankStyles(List.of())).isEmpty();
    }
}
