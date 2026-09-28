package com.ages.pie.application.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.dto.user.StyleIdentificationResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StyleIdentificationService {

    private static final int ANSWER_WEIGHT = 2;
    private static final int PREFERENCE_WEIGHT = 1;

    private static final Map<ProductStyle, int[]> REFERENCE_COLORS = Map.of(
            ProductStyle.ROMANTICO, new int[] { 0xE91E63, 0xF5F0E6 },
            ProductStyle.CLASSICO, new int[] { 0xFFFFFF, 0x808080, 0x2D5BA3 },
            ProductStyle.CASUAL, new int[] { 0x4F86C6 },
            ProductStyle.CRIATIVO, new int[] { 0x2E7D32, 0xE2725B },
            ProductStyle.DRAMATICO, new int[] { 0x000000, 0xC62828 },
            ProductStyle.REFINADO, new int[] { 0xE3C99F });

    private final BodyProfileRepository bodyProfileRepository;
    private final UserRepository userRepository;

    public StyleIdentificationService(BodyProfileRepository bodyProfileRepository,
            UserRepository userRepository) {
        this.bodyProfileRepository = bodyProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StyleIdentificationResponseDTO identify(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + userId);
        }

        Optional<BodyProfile> profile = bodyProfileRepository.findByCustomerId(userId);
        if (profile.isEmpty()) {
            return new StyleIdentificationResponseDTO(List.of());
        }

        BodyProfile bodyProfile = profile.get();
        Map<ProductStyle, Integer> scores = new EnumMap<>(ProductStyle.class);
        scoreAnswers(bodyProfile, scores);
        scorePreferences(bodyProfile, scores);

        if (scores.isEmpty()) {
            return new StyleIdentificationResponseDTO(List.of());
        }

        ProductStyle identified = highestScored(scores);
        bodyProfile.setIdentifiedStyle(identified.getId());
        bodyProfileRepository.save(bodyProfile);

        return new StyleIdentificationResponseDTO(List.of(identified.getId()));
    }

    private void scoreAnswers(BodyProfile bodyProfile, Map<ProductStyle, Integer> scores) {
        String[] answers = bodyProfile.getStylePreference();
        if (answers == null) {
            return;
        }
        Arrays.stream(answers)
                .map(ProductStyle::fromId)
                .flatMap(Optional::stream)
                .forEach(style -> scores.merge(style, ANSWER_WEIGHT, Integer::sum));
    }

    private void scorePreferences(BodyProfile bodyProfile, Map<ProductStyle, Integer> scores) {
        String[] favoriteColors = bodyProfile.getFavoriteColors();
        if (favoriteColors == null) {
            return;
        }
        Arrays.stream(favoriteColors)
                .map(this::toRgb)
                .flatMap(Optional::stream)
                .map(this::closestStyle)
                .forEach(style -> scores.merge(style, PREFERENCE_WEIGHT, Integer::sum));
    }

    private Optional<Integer> toRgb(String hex) {
        if (hex == null || !hex.matches("^#[0-9A-Fa-f]{6}$")) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(hex.substring(1), 16));
    }

    private ProductStyle closestStyle(int rgb) {
        ProductStyle closest = null;
        long shortestDistance = Long.MAX_VALUE;

        for (ProductStyle style : ProductStyle.values()) {
            for (int reference : REFERENCE_COLORS.get(style)) {
                long distance = squaredDistance(rgb, reference);
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    closest = style;
                }
            }
        }
        return closest;
    }

    private long squaredDistance(int first, int second) {
        long red = ((first >> 16) & 0xFF) - ((second >> 16) & 0xFF);
        long green = ((first >> 8) & 0xFF) - ((second >> 8) & 0xFF);
        long blue = (first & 0xFF) - (second & 0xFF);
        return red * red + green * green + blue * blue;
    }

    private ProductStyle highestScored(Map<ProductStyle, Integer> scores) {
        ProductStyle highest = null;
        for (ProductStyle style : ProductStyle.values()) {
            Integer score = scores.get(style);
            if (score != null && (highest == null || score > scores.get(highest))) {
                highest = style;
            }
        }
        return highest;
    }
}
