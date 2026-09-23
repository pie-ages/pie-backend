package com.ages.pie.application.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.user.UserStyleResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserStyleService {

    private final UserRepository userRepository;
    private final BodyProfileRepository bodyProfileRepository;

    public UserStyleService(UserRepository userRepository,
            BodyProfileRepository bodyProfileRepository) {
        this.userRepository = userRepository;
        this.bodyProfileRepository = bodyProfileRepository;
    }

    @Transactional(readOnly = true)
    public UserStyleResponseDTO getMyStyle(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw userNotFound(userId);
        }

        List<String> styles = bodyProfileRepository.findByCustomerId(userId)
                .map(BodyProfile::getStylePreference)
                .map(Arrays::asList)
                .orElse(List.of());

        return new UserStyleResponseDTO(styles);
    }

    @Transactional
    public UserStyleResponseDTO updateMyStyle(UUID userId, List<String> styles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> userNotFound(userId));

        List<String> normalized = normalize(styles);

        BodyProfile profile = bodyProfileRepository.findByCustomerId(userId)
                .orElseGet(() -> new BodyProfile(user));
        profile.updateStylePreference(normalized.toArray(new String[0]));
        bodyProfileRepository.save(profile);

        return new UserStyleResponseDTO(normalized);
    }

    private List<String> normalize(List<String> styles) {
        if (styles == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estilos são obrigatórios");
        }

        List<String> invalid = styles.stream()
                .filter(s -> !ProductStyle.isValid(s))
                .toList();
        if (!invalid.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estilo(s) inválido(s): " + invalid);
        }

        return styles.stream()
                .map(this::canonicalId)
                .toList();
    }

    private String canonicalId(String value) {
        for (ProductStyle style : ProductStyle.values()) {
            if (style.getId().equalsIgnoreCase(value.trim())) {
                return style.getId();
            }
        }
        return value.trim();
    }

    private ResourceNotFoundException userNotFound(UUID userId) {
        return new ResourceNotFoundException("Usuário não encontrado: " + userId);
    }
}
