package com.ages.pie.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.dto.user.UserStyleResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

    @Mock
    private BodyProfileRepository bodyProfileRepository;

    @InjectMocks
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
    }

    @Test
    void getMyStyle_shouldReturnStyles_whenProfileExists() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        UserStyleResponseDTO result = userStyleService.getMyStyle(userId);

        assertThat(result.styles()).containsExactly("casual");
    }

    @Test
    void getMyStyle_shouldReturnEmpty_whenNoProfile() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.empty());

        UserStyleResponseDTO result = userStyleService.getMyStyle(userId);

        assertThat(result.styles()).isEmpty();
    }

    @Test
    void getMyStyle_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userStyleService.getMyStyle(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void updateMyStyle_shouldPersistNormalizedStyles_whenProfileExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));
        when(bodyProfileRepository.save(any(BodyProfile.class))).thenAnswer(i -> i.getArgument(0));

        UserStyleResponseDTO result =
                userStyleService.updateMyStyle(userId, List.of("CASUAL", "ROMANTICO"));

        assertThat(result.styles()).containsExactly("casual", "romantico");
        ArgumentCaptor<BodyProfile> captor = ArgumentCaptor.forClass(BodyProfile.class);
        verify(bodyProfileRepository).save(captor.capture());
        assertThat(captor.getValue().getStylePreference())
                .containsExactly("casual", "romantico");
    }

    @Test
    void updateMyStyle_shouldCreateProfile_whenNoProfileExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.empty());
        when(bodyProfileRepository.save(any(BodyProfile.class))).thenAnswer(i -> i.getArgument(0));

        UserStyleResponseDTO result =
                userStyleService.updateMyStyle(userId, List.of("casual"));

        assertThat(result.styles()).containsExactly("casual");
        verify(bodyProfileRepository).save(any(BodyProfile.class));
    }

    @Test
    void updateMyStyle_shouldThrowBadRequest_whenStyleIsInvalid() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userStyleService.updateMyStyle(userId, List.of("PUNK")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("inválido");

        verify(bodyProfileRepository, never()).save(any());
    }

    @Test
    void updateMyStyle_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userStyleService.updateMyStyle(userId, List.of("casual")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(bodyProfileRepository, never()).save(any());
    }
}
