package com.ages.pie.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.dto.user.UserStyleResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStyleServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserStyleService userStyleService;

    private UUID userId;
    private User user;
    private BodyProfile profile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User("Ana Silva", "ana@email.com", "hash(senha123)");
        profile = new BodyProfile(user);
        profile.updateStylePreference(new String[]{"casual"});
        userStyleService = new UserStyleService(userRepository);
    }

    @Test
    void getMyStyle_shouldReturnStyles_whenUserHasManualStyles() {
        user.updateStyles(List.of(ProductStyle.CASUAL));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserStyleResponseDTO result = userStyleService.getMyStyle(userId);

        assertThat(result.styles()).containsExactly(ProductStyle.CASUAL);
    }

    @Test
    void getMyStyle_shouldReturnEmpty_whenUserHasNoManualStyles() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserStyleResponseDTO result = userStyleService.getMyStyle(userId);

        assertThat(result.styles()).isEmpty();
    }

    @Test
    void getMyStyle_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userStyleService.getMyStyle(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void updateMyStyle_shouldPersistEnumStyles_withoutChangingQuestionnaireProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserStyleResponseDTO result =
                userStyleService.updateMyStyle(userId, List.of("CASUAL", "ROMANTICO"));

        assertThat(result.styles()).containsExactly(ProductStyle.CASUAL, ProductStyle.ROMANTICO);
        verify(userRepository).save(user);
        assertThat(user.getStyles()).containsExactly(ProductStyle.CASUAL, ProductStyle.ROMANTICO);
        assertThat(profile.getStylePreference()).containsExactly("casual");
    }

    @Test
    void updateMyStyle_shouldPersistStyles_whenUserHasNoManualStyles() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserStyleResponseDTO result =
                userStyleService.updateMyStyle(userId, List.of("casual"));

        assertThat(result.styles()).containsExactly(ProductStyle.CASUAL);
        verify(userRepository).save(user);
    }

    @Test
    void updateMyStyle_shouldThrowBadRequest_whenStyleIsInvalid() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userStyleService.updateMyStyle(userId, List.of("PUNK")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("inválido");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMyStyle_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userStyleService.updateMyStyle(userId, List.of("casual")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository, never()).save(any());
    }
}
