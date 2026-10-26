package com.ages.pie.domain.service;

import com.ages.pie.domain.service.SeasonCatalog.Season;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeasonCatalogTest {

    private static final List<String> SEASON_IDS = List.of(
            "soft_summer", "light_summer", "true_summer", "light_spring", "bright_spring", "true_spring",
            "soft_autumn", "deep_autumn", "true_autumn", "bright_winter", "deep_winter", "true_winter");

    private final SeasonCatalog catalog = new SeasonCatalog();

    @Test
    void mapsEveryAxesCombinationToADistinctSeason() {
        List<String> ids = SeasonCatalog.TEMPERATURES.stream()
                .flatMap(t -> SeasonCatalog.DEPTHS.stream()
                        .flatMap(d -> SeasonCatalog.CHROMAS.stream().map(c -> catalog.findByAxes(t, d, c))))
                .map(Season::id)
                .toList();
        assertThat(ids).containsExactlyInAnyOrderElementsOf(SEASON_IDS);
    }

    @Test
    void everySeasonHasFourHexHighlightsInsideItsPalette() {
        for (String id : SEASON_IDS) {
            Season season = catalog.findById(id).orElseThrow();
            assertThat(season.highlightColors()).hasSize(4).allMatch(color -> color.matches("^#[0-9A-F]{6}$"));
            assertThat(season.palette()).containsAll(season.highlightColors());
        }
    }

    @Test
    void oppositeInvertsAllAxesAndIsSymmetric() {
        Season softAutumn = catalog.findById("soft_autumn").orElseThrow();
        Season opposite = catalog.opposite(softAutumn);

        assertThat(opposite.id()).isEqualTo("true_winter");
        for (String id : SEASON_IDS) {
            Season season = catalog.findById(id).orElseThrow();
            assertThat(catalog.opposite(catalog.opposite(season))).isEqualTo(season);
        }
    }

    @Test
    void unknownOrMissingValuesFindNothing() {
        assertThat(catalog.findByAxes("neutral", "medium", "soft")).isNull();
        assertThat(catalog.findByAxes(null, null, null)).isNull();
        assertThat(catalog.findById("not_a_season")).isEmpty();
        assertThat(catalog.findById(null)).isEmpty();
    }

    @Test
    void failsAtStartupWhenChartDoesNotCoverEveryCombination() {
        assertThatThrownBy(() -> new SeasonCatalog("colorimetry/incomplete-seasons.json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("covers 1 axes combinations; expected 12");
    }
}
