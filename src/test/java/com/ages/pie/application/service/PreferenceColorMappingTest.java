package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.PreferencesRequestDTO;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ProductColor;
import com.ages.pie.domain.service.SeasonCatalog;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreferenceColorMappingTest {

    @Mock
    private BodyProfileRepository bodyProfileRepository;

    @Mock
    private UserRepository userRepository;

    private PreferenceService service;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new PreferenceService(
                bodyProfileRepository,
                userRepository,
                new SeasonCatalog()
        );

        userId = UUID.randomUUID();
        user = new User("Ana Silva", "ana@email.com", UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    }

    @Test
    void shouldPreserveHexColorsAndSaveUniqueCategories() {
        BodyProfile profile = new BodyProfile(user);
        when(bodyProfileRepository.findByCustomerId(userId))
                .thenReturn(Optional.of(profile));

        var result = service.updateFavoriteColors(
                userId,
                new PreferencesRequestDTO(
                        List.of("#1241b6", "#1241B6", "#173AA0", "#FF0000")
                )
        );

        assertThat(result.favoriteColors())
                .containsExactly("#1241B6", "#173AA0", "#FF0000");

        assertThat(profile.getFavoriteColors())
                .containsExactly("#1241B6", "#173AA0", "#FF0000");

        assertThat(user.getFavoriteColors())
                .containsExactly(ProductColor.AZUL_MARINHO, ProductColor.VERMELHO);

        verify(userRepository).save(user);
        verify(bodyProfileRepository).save(profile);
    }

    @Test
    void shouldClearBothRepresentations() {
        BodyProfile profile = new BodyProfile(user);
        profile.setFavoriteColors(new String[]{"#FF0000"});
        user.updateFavoriteColors(List.of(ProductColor.VERMELHO));

        when(bodyProfileRepository.findByCustomerId(userId))
                .thenReturn(Optional.of(profile));

        var result = service.updateFavoriteColors(
                userId,
                new PreferencesRequestDTO(List.of())
        );

        assertThat(result.favoriteColors()).isEmpty();
        assertThat(profile.getFavoriteColors()).isEmpty();
        assertThat(user.getFavoriteColors()).isEmpty();

        verify(userRepository).save(user);
        verify(bodyProfileRepository).save(profile);
    }

    @Test
    void shouldCreateProfileAndClassifyColors() {
        when(bodyProfileRepository.findByCustomerId(userId))
                .thenReturn(Optional.empty());

        service.updateFavoriteColors(
                userId,
                new PreferencesRequestDTO(List.of("#FFFFFF"))
        );

        ArgumentCaptor<BodyProfile> captor =
                ArgumentCaptor.forClass(BodyProfile.class);

        verify(bodyProfileRepository).save(captor.capture());
        verify(userRepository).save(user);

        assertThat(captor.getValue().getFavoriteColors())
                .containsExactly("#FFFFFF");

        assertThat(user.getFavoriteColors())
                .containsExactly(ProductColor.BRANCO);
    }

    @Test
    void shouldReplacePreviousCategoriesWhenEditing() {
        BodyProfile profile = new BodyProfile(user);
        profile.setFavoriteColors(new String[]{"#FF0000"});
        user.updateFavoriteColors(List.of(ProductColor.VERMELHO));

        when(bodyProfileRepository.findByCustomerId(userId))
                .thenReturn(Optional.of(profile));

        service.updateFavoriteColors(
                userId,
                new PreferencesRequestDTO(List.of("#0000FF"))
        );

        assertThat(profile.getFavoriteColors()).containsExactly("#0000FF");
        assertThat(user.getFavoriteColors()).containsExactly(ProductColor.AZUL);
    }

    @Test
    void shouldSaveNewColorCategories() {
        BodyProfile profile = new BodyProfile(user);
        when(bodyProfileRepository.findByCustomerId(userId))
                .thenReturn(Optional.of(profile));

        var result = service.updateFavoriteColors(
                userId,
                new PreferencesRequestDTO(
                        List.of("#FFFF00", "#FFA500", "#87CEFA", "#98FF98")
                )
        );

        assertThat(result.favoriteColors())
                .containsExactly("#FFFF00", "#FFA500", "#87CEFA", "#98FF98");

        assertThat(profile.getFavoriteColors())
                .containsExactly("#FFFF00", "#FFA500", "#87CEFA", "#98FF98");

        assertThat(user.getFavoriteColors())
                .containsExactly(
                        ProductColor.AMARELO,
                        ProductColor.LARANJA,
                        ProductColor.AZUL_CLARO,
                        ProductColor.VERDE_MENTA
                );

        verify(userRepository).save(user);
        verify(bodyProfileRepository).save(profile);
    }
}