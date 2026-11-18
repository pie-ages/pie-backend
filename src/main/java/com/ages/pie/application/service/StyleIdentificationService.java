package com.ages.pie.application.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.user.StyleIdentificationResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.StyleAnswer;
import com.ages.pie.domain.entity.StyleOption;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.Style;
import com.ages.pie.domain.enums.StyleAnswerType;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.StyleAnswerRepository;
import com.ages.pie.infrastructure.repository.StyleOptionRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StyleIdentificationService {

    private final BodyProfileRepository bodyProfileRepository;
    private final UserRepository userRepository;
    private final StyleAnswerRepository styleAnswerRepository;
    private final StyleOptionRepository styleOptionRepository;

    public StyleIdentificationService(BodyProfileRepository bodyProfileRepository,
            UserRepository userRepository, StyleAnswerRepository styleAnswerRepository,
            StyleOptionRepository styleOptionRepository) {
        this.bodyProfileRepository = bodyProfileRepository;
        this.userRepository = userRepository;
        this.styleAnswerRepository = styleAnswerRepository;
        this.styleOptionRepository = styleOptionRepository;
    }

    @Transactional
    public StyleIdentificationResponseDTO identify(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));
        List<Style> votes = new ArrayList<>();
        for (StyleAnswer answer : styleAnswerRepository.findByCustomerId(userId)) {
            if (!answer.getQuestion().isActive()) continue;
            if (answer.getAnswerType() == StyleAnswerType.OPTION) {
                votes.add(answer.getOption().getStyle());
            } else if (answer.getAnswerType() == StyleAnswerType.BOTH) {
                styleOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(answer.getQuestion().getId())
                        .stream().limit(2).map(StyleOption::getStyle)
                        .forEach(votes::add);
            }
        }

        List<Style> ranked = StyleScoreCalculator.rankStyles(votes);
        if (ranked.isEmpty()) {
            return new StyleIdentificationResponseDTO(List.of(), List.of());
        }
        Style identified = ranked.getFirst();
        List<String> touches = ranked.stream().skip(1).limit(2).map(Style::name).toList();
        var profile = bodyProfileRepository.findByCustomerId(userId);
        BodyProfile bodyProfile = profile.orElseGet(() -> new BodyProfile(user));
        bodyProfile.setIdentifiedStyle(identified.name());
        bodyProfile.setIdentifiedStyleTouches(touches.toArray(String[]::new));
        bodyProfileRepository.save(bodyProfile);
        return new StyleIdentificationResponseDTO(List.of(identified.name()), touches);
    }

    @Transactional(readOnly = true)
    public StyleIdentificationResponseDTO getIdentified(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + userId);
        }
        return bodyProfileRepository.findByCustomerId(userId).map(profile -> {
            Style essence = parseStyle(profile.getIdentifiedStyle());
            if (essence == null) return new StyleIdentificationResponseDTO(List.of(), List.of());
            String[] savedTouches = profile.getIdentifiedStyleTouches();
            List<String> touches = Arrays.stream(savedTouches)
                    .map(this::parseStyle).filter(style -> style != null && style != essence)
                    .distinct().limit(2).map(Style::name).toList();
            return new StyleIdentificationResponseDTO(List.of(essence.name()), touches);
        }).orElseGet(() -> new StyleIdentificationResponseDTO(List.of(), List.of()));
    }

    private Style parseStyle(String value) {
        if (value == null) return null;
        return Style.valueOf(value);
    }
}
