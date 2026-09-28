package com.ages.pie.application.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
        Map<Style, Integer> answerScores = new EnumMap<>(Style.class);
        for (StyleAnswer answer : styleAnswerRepository.findByCustomerId(userId)) {
            if (answer.getAnswerType() == StyleAnswerType.OPTION) {
                answerScores.merge(answer.getOption().getStyle(), 1, Integer::sum);
            } else if (answer.getAnswerType() == StyleAnswerType.BOTH) {
                styleOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(answer.getQuestion().getId())
                        .stream().limit(2).map(StyleOption::getStyle)
                        .forEach(style -> answerScores.merge(style, 1, Integer::sum));
            }
        }

        Style identified = null;
        for (Style style : Style.values()) {
            int score = answerScores.getOrDefault(style, 0);
            if (score > 0 && (identified == null || score > answerScores.get(identified))) {
                identified = style;
            }
        }

        if (identified == null) {
            return new StyleIdentificationResponseDTO(List.of());
        }
        var profile = bodyProfileRepository.findByCustomerId(userId);
        BodyProfile bodyProfile = profile.orElseGet(() -> new BodyProfile(user));
        bodyProfile.setIdentifiedStyle(identified.name());
        bodyProfileRepository.save(bodyProfile);
        return new StyleIdentificationResponseDTO(List.of(identified.name()));
    }

    @Transactional(readOnly = true)
    public StyleIdentificationResponseDTO getIdentified(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + userId);
        }
        String identified = bodyProfileRepository.findByCustomerId(userId)
                .map(BodyProfile::getIdentifiedStyle).orElse(null);
        for (Style style : Style.values()) {
            if (style.name().equals(identified)) {
                return new StyleIdentificationResponseDTO(List.of(style.name()));
            }
        }
        return new StyleIdentificationResponseDTO(List.of());
    }
}