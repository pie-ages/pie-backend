package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.PreferencesRequestDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreferenceServiceTest {

    @Mock
    private BodyProfileRepository bodyProfileRepository;

    @Mock
    private UserRepository userRepository;

    private PreferenceService preferenceService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        // Random com seed fixa para testes determinísticos
        preferenceService = new PreferenceService(bodyProfileRepository, userRepository, new Random(42));

        userId = UUID.randomUUID();
        user = new User("Ana Silva", "ana@email.com", "hash(senha123)");
        ReflectionTestUtils.setField(user, "id", userId);
    }

    @Test
    void getPreferences_shouldReturnFavoriteColors_whenUserHasBodyProfile() {
        BodyProfile profile = new BodyProfile(user);
        profile.setFavoriteColors(new String[]{"#FF0000", "#00FF00"});

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        PreferencesResponseDTO result = preferenceService.getPreferences(userId);

        assertThat(result.favoriteColors()).containsExactly("#FF0000", "#00FF00");
        assertThat(result.highlightColors()).hasSize(4);
        assertThat(result.avoidColors()).hasSize(4);
    }

    @Test
    void getPreferences_shouldReturnEmptyFavorites_whenUserHasNoBodyProfile() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.empty());

        PreferencesResponseDTO result = preferenceService.getPreferences(userId);

        assertThat(result.favoriteColors()).isEmpty();
        assertThat(result.highlightColors()).hasSize(4);
        assertThat(result.avoidColors()).hasSize(4);
    }

    @Test
    void getPreferences_shouldReturnEmptyFavorites_whenFavoriteColorsIsNull() {
        BodyProfile profile = new BodyProfile(user);
        // favoriteColors é null por padrão

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        PreferencesResponseDTO result = preferenceService.getPreferences(userId);

        assertThat(result.favoriteColors()).isEmpty();
    }

    @Test
    void getPreferences_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> preferenceService.getPreferences(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void getPreferences_shouldReturnRandomHighlightAndAvoidColors() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.empty());

        PreferencesResponseDTO result = preferenceService.getPreferences(userId);

        // Cores devem estar no formato hexadecimal #RRGGBB
        for (String color : result.highlightColors()) {
            assertThat(color).matches("^#[0-9A-F]{6}$");
        }
        for (String color : result.avoidColors()) {
            assertThat(color).matches("^#[0-9A-F]{6}$");
        }
    }

    @Test
    void updateFavoriteColors_shouldPersistColors_whenUserHasBodyProfile() {
        BodyProfile profile = new BodyProfile(user);
        PreferencesRequestDTO dto = new PreferencesRequestDTO(List.of("#AABBCC", "#112233"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));
        when(bodyProfileRepository.save(any(BodyProfile.class))).thenReturn(profile);

        PreferencesResponseDTO result = preferenceService.updateFavoriteColors(userId, dto);

        assertThat(result.favoriteColors()).containsExactly("#AABBCC", "#112233");
        verify(bodyProfileRepository).save(profile);
    }

    @Test
    void updateFavoriteColors_shouldCreateBodyProfile_whenUserHasNoProfile() {
        PreferencesRequestDTO dto = new PreferencesRequestDTO(List.of("#AABBCC"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.empty());
        when(bodyProfileRepository.save(any(BodyProfile.class))).thenAnswer(call -> call.getArgument(0));

        PreferencesResponseDTO result = preferenceService.updateFavoriteColors(userId, dto);

        assertThat(result.favoriteColors()).containsExactly("#AABBCC");
        verify(bodyProfileRepository).save(any(BodyProfile.class));
    }

    @Test
    void updateFavoriteColors_shouldRemoveDuplicateColors() {
        BodyProfile profile = new BodyProfile(user);
        PreferencesRequestDTO dto = new PreferencesRequestDTO(List.of("#AABBCC", "#AABBCC", "#112233"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));
        when(bodyProfileRepository.save(any(BodyProfile.class))).thenReturn(profile);

        PreferencesResponseDTO result = preferenceService.updateFavoriteColors(userId, dto);

        assertThat(result.favoriteColors()).containsExactly("#AABBCC", "#112233");
    }

    @Test
    void updateFavoriteColors_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        PreferencesRequestDTO dto = new PreferencesRequestDTO(List.of("#AABBCC"));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preferenceService.updateFavoriteColors(userId, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void updateFavoriteColors_shouldNotAlterHighlightOrAvoidColors() {
        BodyProfile profile = new BodyProfile(user);
        PreferencesRequestDTO dto = new PreferencesRequestDTO(List.of("#AABBCC"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));
        when(bodyProfileRepository.save(any(BodyProfile.class))).thenReturn(profile);

        PreferencesResponseDTO result = preferenceService.updateFavoriteColors(userId, dto);

        // highlight e avoid continuam sendo geradas aleatoriamente (não salvas)
        assertThat(result.highlightColors()).hasSize(4);
        assertThat(result.avoidColors()).hasSize(4);
        // favoritas são só as que o usuário mandou
        assertThat(result.favoriteColors()).containsExactly("#AABBCC");
    }

    @Test
    void generateRandomColors_shouldReturnCorrectCount() {
        List<String> colors = preferenceService.generateRandomColors(4);
        assertThat(colors).hasSize(4);
    }

    @Test
    void generateRandomColors_shouldReturnValidHexColors() {
        List<String> colors = preferenceService.generateRandomColors(10);
        for (String color : colors) {
            assertThat(color).matches("^#[0-9A-F]{6}$");
        }
    }
}
