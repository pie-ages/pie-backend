package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.ColorimetrySeasonDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ColorimetrySeason;
import com.ages.pie.infrastructure.ai.BedrockImageAnalysisClient;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ColorimetryService {

    private final BedrockImageAnalysisClient bedrockClient;
    private final BodyProfileRepository bodyProfileRepository;
    private final UserRepository userRepository;

    public ColorimetryService(BedrockImageAnalysisClient bedrockClient,
                               BodyProfileRepository bodyProfileRepository,
                               UserRepository userRepository) {
        this.bedrockClient = bedrockClient;
        this.bodyProfileRepository = bodyProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PreferencesResponseDTO analyze(UUID userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível ler a imagem enviada.");
        }

        String format = resolveFormat(file.getOriginalFilename(), file.getContentType());
        String seasonId = bedrockClient.analyzeColorimetry(bytes, format);

        BodyProfile profile = bodyProfileRepository.findByCustomerId(userId)
                .orElseGet(() -> new BodyProfile(user));

        ColorimetrySeason season = parseSeason(seasonId);
        if (season != null) {
            profile.setColorPalette(season.name());
            bodyProfileRepository.save(profile);
        }

        return buildResponse(season, profile);
    }

    private static ColorimetrySeason parseSeason(String seasonId) {
        if (seasonId == null) return null;
        try {
            return ColorimetrySeason.valueOf(seasonId.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
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

    private static String resolveFormat(String filename, String contentType) {
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "jpeg";
            if (lower.endsWith(".png")) return "png";
            if (lower.endsWith(".webp")) return "webp";
            if (lower.endsWith(".gif")) return "gif";
        }
        if (contentType != null) {
            if (contentType.contains("jpeg")) return "jpeg";
            if (contentType.contains("png")) return "png";
            if (contentType.contains("webp")) return "webp";
            if (contentType.contains("gif")) return "gif";
        }
        return "jpeg";
    }
}
