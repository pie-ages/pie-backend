package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.ColorimetrySeasonDTO;
import com.ages.pie.application.dto.user.PreferencesRequestDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ColorimetrySeason;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class PreferenceService {

    private final BodyProfileRepository bodyProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public PreferenceService(BodyProfileRepository bodyProfileRepository,
                             UserRepository userRepository) {
        this.bodyProfileRepository = bodyProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PreferencesResponseDTO getPreferences(UUID userId) {
        verifyUserExists(userId);
        BodyProfile profile = bodyProfileRepository.findByCustomerId(userId).orElse(null);
        return buildResponse(parseSeason(profile != null ? profile.getColorPalette() : null), profile);
    }

    @Transactional
    public PreferencesResponseDTO updateFavoriteColors(UUID userId, PreferencesRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        BodyProfile profile = bodyProfileRepository.findByCustomerId(userId)
                .orElseGet(() -> new BodyProfile(user));

        List<String> uniqueColors = dto.favoriteColors().stream()
                .distinct()
                .toList();

        profile.setFavoriteColors(uniqueColors.toArray(new String[0]));
        bodyProfileRepository.save(profile);

        return buildResponse(parseSeason(profile.getColorPalette()), profile);
    }

    private void verifyUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + userId);
        }
    }

    private static PreferencesResponseDTO buildResponse(ColorimetrySeason season, BodyProfile profile) {
        List<String> highlight = season != null ? season.getHighlightColors() : Collections.emptyList();
        List<String> avoid = season != null ? season.getAvoidColors() : Collections.emptyList();
        List<String> favorites = profile != null && profile.getFavoriteColors() != null
                ? Arrays.asList(profile.getFavoriteColors())
                : Collections.emptyList();
        ColorimetrySeasonDTO seasonDTO = season != null ? ColorimetrySeasonDTO.from(season) : null;
        return new PreferencesResponseDTO(highlight, avoid, favorites, seasonDTO);
    }

    private static ColorimetrySeason parseSeason(String colorPalette) {
        if (colorPalette == null) return null;
        try {
            return ColorimetrySeason.valueOf(colorPalette.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
