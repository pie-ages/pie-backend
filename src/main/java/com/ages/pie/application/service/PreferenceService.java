package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.PreferencesRequestDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class PreferenceService {

    private final BodyProfileRepository bodyProfileRepository;
    private final UserRepository userRepository;
    private final Random random;

    @Autowired
    public PreferenceService(BodyProfileRepository bodyProfileRepository,
                             UserRepository userRepository) {
        this.bodyProfileRepository = bodyProfileRepository;
        this.userRepository = userRepository;
        this.random = new Random();
    }

    // Construtor para testes (permite injetar Random controlado)
    PreferenceService(BodyProfileRepository bodyProfileRepository,
                      UserRepository userRepository,
                      Random random) {
        this.bodyProfileRepository = bodyProfileRepository;
        this.userRepository = userRepository;
        this.random = random;
    }

    @Transactional(readOnly = true)
    public PreferencesResponseDTO getPreferences(UUID userId) {
        verifyUserExists(userId);

        List<String> favoriteColors = bodyProfileRepository.findByCustomerId(userId)
                .map(BodyProfile::getFavoriteColors)
                .map(colors -> colors != null ? Arrays.asList(colors) : Collections.<String>emptyList())
                .orElse(Collections.emptyList());

        return new PreferencesResponseDTO(
                generateRandomColors(4),
                generateRandomColors(4),
                favoriteColors
        );
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

        return new PreferencesResponseDTO(
                generateRandomColors(4),
                generateRandomColors(4),
                uniqueColors
        );
    }

    private void verifyUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + userId);
        }
    }

    List<String> generateRandomColors(int count) {
        List<String> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            colors.add(String.format("#%06X", random.nextInt(0xFFFFFF + 1)));
        }
        return colors;
    }
}
