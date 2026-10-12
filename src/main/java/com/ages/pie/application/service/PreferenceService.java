package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.PreferencesRequestDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.service.SeasonCatalog;
import com.ages.pie.domain.service.SeasonCatalog.Season;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PreferenceService {

    private final BodyProfileRepository bodyProfileRepository;
    private final UserRepository userRepository;
    private final SeasonCatalog seasonCatalog;

    public PreferenceService(BodyProfileRepository bodyProfileRepository,
                             UserRepository userRepository,
                             SeasonCatalog seasonCatalog) {
        this.bodyProfileRepository = bodyProfileRepository;
        this.userRepository = userRepository;
        this.seasonCatalog = seasonCatalog;
    }

    @Transactional(readOnly = true)
    public PreferencesResponseDTO getPreferences(UUID userId) {
        verifyUserExists(userId);
        return toResponse(bodyProfileRepository.findByCustomerId(userId));
    }

    @Transactional
    public PreferencesResponseDTO updateFavoriteColors(UUID userId, PreferencesRequestDTO dto) {
        BodyProfile profile = findOrCreateProfile(userId);

        List<String> uniqueColors = dto.favoriteColors().stream()
                .distinct()
                .toList();

        profile.setFavoriteColors(uniqueColors.toArray(new String[0]));
        bodyProfileRepository.save(profile);

        return toResponse(Optional.of(profile));
    }

    @Transactional
    public PreferencesResponseDTO saveSeason(UUID userId, Season season) {
        BodyProfile profile = findOrCreateProfile(userId);
        profile.setColorPalette(season.id());
        bodyProfileRepository.save(profile);
        return toResponse(Optional.of(profile));
    }

    private PreferencesResponseDTO toResponse(Optional<BodyProfile> profile) {
        List<String> favoriteColors = profile
                .map(BodyProfile::getFavoriteColors)
                .map(Arrays::asList)
                .orElse(List.of());
        Optional<Season> season = profile
                .map(BodyProfile::getColorPalette)
                .flatMap(seasonCatalog::findById);

        return new PreferencesResponseDTO(
                season.map(Season::highlightColors).orElse(List.of()),
                season.map(s -> seasonCatalog.opposite(s).highlightColors()).orElse(List.of()),
                favoriteColors
        );
    }

    private BodyProfile findOrCreateProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));
        return bodyProfileRepository.findByCustomerId(userId)
                .orElseGet(() -> new BodyProfile(user));
    }

    private void verifyUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + userId);
        }
    }
}
