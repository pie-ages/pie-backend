package com.ages.pie.application.service;

import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.user.UserStyleResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserStyleService {

    private final UserRepository userRepository;

    public UserStyleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserStyleResponseDTO getMyStyle(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> userNotFound(userId));
        List<ProductStyle> styles = user.getStyles();
        return new UserStyleResponseDTO(styles == null ? List.of() : styles);
    }

    @Transactional
    public UserStyleResponseDTO updateMyStyle(UUID userId, List<String> styles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> userNotFound(userId));

        List<ProductStyle> normalized = normalize(styles);
        user.updateStyles(normalized);
        userRepository.save(user);

        return new UserStyleResponseDTO(normalized);
    }

    private List<ProductStyle> normalize(List<String> styles) {
        if (styles == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estilos são obrigatórios");
        }

        return styles.stream()
                .map(this::parseStyle)
                .toList();
    }

    private ProductStyle parseStyle(String value) {
        if (value == null || value.isBlank()) {
            throw invalidStyle(value);
        }
        for (ProductStyle style : ProductStyle.values()) {
            if (style.name().equalsIgnoreCase(value.trim())
                    || style.getId().equalsIgnoreCase(value.trim())) {
                return style;
            }
        }
        throw invalidStyle(value);
    }

    private ResponseStatusException invalidStyle(String value) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Estilo inválido: " + value);
    }

    private ResourceNotFoundException userNotFound(UUID userId) {
        return new ResourceNotFoundException("Usuário não encontrado: " + userId);
    }
}
