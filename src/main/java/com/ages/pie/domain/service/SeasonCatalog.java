package com.ages.pie.domain.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Seasonal color chart (Método Sazonal Expandido) loaded from {@code colorimetry/seasons.json}.
 * Every combination of the three axes maps to exactly one season, so any valid classification has a palette.
 */
@Component
public class SeasonCatalog {

    public static final Set<String> TEMPERATURES = Set.of("cool", "warm");
    public static final Set<String> DEPTHS = Set.of("light", "medium", "deep");
    public static final Set<String> CHROMAS = Set.of("soft", "bright");
    private static final String RESOURCE = "colorimetry/seasons.json";
    private static final Map<String, String> INVERTED = Map.of(
            "cool", "warm", "warm", "cool",
            "light", "deep", "deep", "light", "medium", "medium",
            "soft", "bright", "bright", "soft");

    private final Map<String, Season> byId = new HashMap<>();
    private final Map<Axes, Season> byAxes = new HashMap<>();

    public SeasonCatalog() {
        this(RESOURCE);
    }

    SeasonCatalog(String resource) {
        Chart chart;
        try (InputStream in = new ClassPathResource(resource).getInputStream()) {
            chart = JsonMapper.builder().build().readValue(in, Chart.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read color chart at classpath:" + resource, e);
        }
        for (Season season : chart.seasons()) {
            Axes axes = season.axes();
            if (!TEMPERATURES.contains(axes.temperature()) || !DEPTHS.contains(axes.depth())
                    || !CHROMAS.contains(axes.chroma())) {
                throw new IllegalStateException("Season '" + season.id() + "' has invalid axes " + axes
                        + "; expected temperature in " + TEMPERATURES + ", depth in " + DEPTHS
                        + ", chroma in " + CHROMAS);
            }
            if (season.highlightColors() == null || season.highlightColors().isEmpty()) {
                throw new IllegalStateException("Season '" + season.id() + "' has no highlightColors");
            }
            Season previous = byAxes.put(axes, season);
            if (previous != null) {
                throw new IllegalStateException("Seasons '" + previous.id() + "' and '" + season.id()
                        + "' share axes " + axes + "; each axes combination must map to one season");
            }
            byId.put(season.id(), season);
        }
        int expected = TEMPERATURES.size() * DEPTHS.size() * CHROMAS.size();
        if (byAxes.size() != expected) {
            throw new IllegalStateException("Color chart covers " + byAxes.size() + " axes combinations; expected "
                    + expected + " so every classification maps to a season");
        }
    }

    public Season findByAxes(String temperature, String depth, String chroma) {
        return byAxes.get(new Axes(temperature, depth, chroma));
    }

    public Optional<Season> findById(String id) {
        return Optional.ofNullable(id).map(byId::get);
    }

    /** The season with every axis inverted; its highlight colors are the ones to avoid. */
    public Season opposite(Season season) {
        Axes axes = season.axes();
        return byAxes.get(new Axes(INVERTED.get(axes.temperature()), INVERTED.get(axes.depth()),
                INVERTED.get(axes.chroma())));
    }

    public record Axes(String temperature, String depth, String chroma) {
    }

    public record Season(String id, String label, List<String> aliases, Axes axes,
                         List<String> highlightColors, List<String> palette) {
    }

    record Chart(String method, List<Season> seasons) {
    }
}
