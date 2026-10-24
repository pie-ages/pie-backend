package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.PreferencesRequestDTO;
import com.ages.pie.application.dto.user.PreferencesResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.service.SeasonCatalog;
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

    private final SeasonCatalog seasonCatalog = new SeasonCatalog();

    private PreferenceService preferenceService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        preferenceService = new PreferenceService(bodyProfileRepository, userRepository, seasonCatalog);

        userId = UUID.randomUUID();
        user = new User("Ana Silva", "ana@email.com", UUID.randomUUID());
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
        assertThat(result.highlightColors()).isEmpty();
        assertThat(result.avoidColors()).isEmpty();
    }

    @Test
    void getPreferences_shouldReturnEmptyFavorites_whenUserHasNoBodyProfile() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.empty());

        PreferencesResponseDTO result = preferenceService.getPreferences(userId);

        assertThat(result.favoriteColors()).isEmpty();
        assertThat(result.highlightColors()).isEmpty();
        assertThat(result.avoidColors()).isEmpty();
        assertThat(result.season()).isNull();
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
    void getPreferences_shouldReturnSeasonColorsAndOppositeSeasonAsAvoid() {
        BodyProfile profile = new BodyProfile(user);
        profile.setColorPalette("soft_autumn");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        PreferencesResponseDTO result = preferenceService.getPreferences(userId);

        assertThat(result.highlightColors())
                .isEqualTo(seasonCatalog.findById("soft_autumn").orElseThrow().highlightColors());
        assertThat(result.avoidColors())
                .isEqualTo(seasonCatalog.findById("true_winter").orElseThrow().highlightColors());
        assertThat(result.season().label()).isEqualTo("Outono Suave");
        assertThat(result.season().highlightCaption()).isEqualTo("Tons quentes e suaves valorizam sua pele.");
        assertThat(result.season().avoidCaption()).isEqualTo("Tons frios e vivos competem com a sua coloração natural.");
    }

    @Test
    void getPreferences_shouldDescribeLightAndDeepSeasonsInCaptions() {
        BodyProfile profile = new BodyProfile(user);
        profile.setColorPalette("light_summer");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        PreferencesResponseDTO result = preferenceService.getPreferences(userId);

        assertThat(result.season().highlightCaption()).isEqualTo("Tons frios, claros e suaves valorizam sua pele.");
        assertThat(result.season().avoidCaption())
                .isEqualTo("Tons quentes, profundos e vivos competem com a sua coloração natural.");
    }

    @Test
    void getPreferences_shouldReturnStableColorsAcrossCalls() {
        BodyProfile profile = new BodyProfile(user);
        profile.setColorPalette("deep_winter");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        assertThat(preferenceService.getPreferences(userId)).isEqualTo(preferenceService.getPreferences(userId));
    }

    @Test
    void getPreferences_shouldIgnoreUnknownStoredPalette() {
        BodyProfile profile = new BodyProfile(user);
        profile.setColorPalette("not_a_season");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        PreferencesResponseDTO result = preferenceService.getPreferences(userId);

        assertThat(result.highlightColors()).isEmpty();
        assertThat(result.avoidColors()).isEmpty();
    }

    @Test
    void saveSeason_shouldPersistSeasonIdAndReturnItsColors() {
        BodyProfile profile = new BodyProfile(user);
        SeasonCatalog.Season season = seasonCatalog.findById("light_spring").orElseThrow();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        PreferencesResponseDTO result = preferenceService.saveSeason(userId, season);

        assertThat(profile.getColorPalette()).isEqualTo("light_spring");
        assertThat(result.highlightColors()).isEqualTo(season.highlightColors());
        assertThat(result.avoidColors()).isEqualTo(seasonCatalog.opposite(season).highlightColors());
        verify(bodyProfileRepository).save(profile);
    }

    @Test
    void saveSeason_shouldCreateBodyProfile_whenUserHasNoProfile() {
        SeasonCatalog.Season season = seasonCatalog.findById("light_spring").orElseThrow();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.empty());

        preferenceService.saveSeason(userId, season);

        verify(bodyProfileRepository).save(org.mockito.ArgumentMatchers.argThat(
                saved -> "light_spring".equals(saved.getColorPalette())));
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
    void updateFavoriteColors_shouldKeepSeasonColorsUntouched() {
        BodyProfile profile = new BodyProfile(user);
        profile.setColorPalette("soft_autumn");
        PreferencesRequestDTO dto = new PreferencesRequestDTO(List.of("#AABBCC"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));
        when(bodyProfileRepository.save(any(BodyProfile.class))).thenReturn(profile);

        PreferencesResponseDTO result = preferenceService.updateFavoriteColors(userId, dto);

        assertThat(profile.getColorPalette()).isEqualTo("soft_autumn");
        assertThat(result.highlightColors())
                .isEqualTo(seasonCatalog.findById("soft_autumn").orElseThrow().highlightColors());
        // favoritas são só as que o usuário mandou
        assertThat(result.favoriteColors()).containsExactly("#AABBCC");
    }
}
