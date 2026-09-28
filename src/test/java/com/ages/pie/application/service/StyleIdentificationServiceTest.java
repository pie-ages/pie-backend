package com.ages.pie.application.service;

import com.ages.pie.application.dto.user.StyleIdentificationResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.ProductStyle;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StyleIdentificationServiceTest {

    @Mock
    private BodyProfileRepository bodyProfileRepository;

    @Mock
    private UserRepository userRepository;

    private StyleIdentificationService service;

    private UUID userId;
    private BodyProfile profile;

    @BeforeEach
    void setUp() {
        service = new StyleIdentificationService(bodyProfileRepository, userRepository);
        userId = UUID.randomUUID();
        profile = new BodyProfile(new User("Ana", "ana@pie.com", "hash"));
    }

    @Test
    void identificaEstiloComRespostasValidas() {
        givenExistingUser();
        withAnswers("dramatico", "dramatico", "casual");

        StyleIdentificationResponseDTO response = service.identify(userId);

        assertThat(response.styles()).containsExactly("dramatico");
    }

    @Test
    void preferenciasDesempatamRespostas() {
        givenExistingUser();
        withAnswers("classico", "casual");
        profile.setFavoriteColors(new String[] { "#4F86C6" });

        StyleIdentificationResponseDTO response = service.identify(userId);

        assertThat(response.styles()).containsExactly("casual");
    }

    @Test
    void identificaSomentePorPreferenciasQuandoNaoHaRespostas() {
        givenExistingUser();
        profile.setFavoriteColors(new String[] { "#2E7D32" });

        StyleIdentificationResponseDTO response = service.identify(userId);

        assertThat(response.styles()).containsExactly("criativo");
    }

    @Test
    void identificaSomentePorRespostasQuandoNaoHaPreferencias() {
        givenExistingUser();
        withAnswers("romantico");

        StyleIdentificationResponseDTO response = service.identify(userId);

        assertThat(response.styles()).containsExactly("romantico");
    }

    @Test
    void persisteEstiloIdentificadoNoPerfil() {
        givenExistingUser();
        withAnswers("refinado");

        service.identify(userId);

        assertThat(profile.getIdentifiedStyle()).isEqualTo("refinado");
        verify(bodyProfileRepository).save(profile);
    }

    @Test
    void ignoraValoresQueNaoExistemNoEnum() {
        givenExistingUser();
        withAnswers("Elegante", "Sporty", "casual");

        StyleIdentificationResponseDTO response = service.identify(userId);

        assertThat(response.styles()).containsExactly("casual");
    }

    @Test
    void retornaSomenteValoresValidosDoEnum() {
        givenExistingUser();
        withAnswers("criativo", "criativo");
        profile.setFavoriteColors(new String[] { "#000000", "#FFFFFF" });

        StyleIdentificationResponseDTO response = service.identify(userId);

        assertThat(response.styles()).hasSize(1);
        assertThat(ProductStyle.fromId(response.styles().getFirst())).isPresent();
    }

    @Test
    void retornaVazioSemPersistirQuandoNaoHaDadosAproveitaveis() {
        givenExistingUser();
        withAnswers("Elegante");
        profile.setFavoriteColors(new String[] { "vermelho" });

        StyleIdentificationResponseDTO response = service.identify(userId);

        assertThat(response.styles()).isEmpty();
        verify(bodyProfileRepository, never()).save(any());
    }

    @Test
    void naoSobrescreveEstiloDefinidoManualmenteQuandoNaoHaDados() {
        givenExistingUser();
        profile.setIdentifiedStyle("dramatico");

        StyleIdentificationResponseDTO response = service.identify(userId);

        assertThat(response.styles()).isEmpty();
        assertThat(profile.getIdentifiedStyle()).isEqualTo("dramatico");
        verify(bodyProfileRepository, never()).save(any());
    }

    @Test
    void retornaVazioQuandoUsuarioNaoPossuiPerfil() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.empty());

        StyleIdentificationResponseDTO response = service.identify(userId);

        assertThat(response.styles()).isEmpty();
        verify(bodyProfileRepository, never()).save(any());
    }

    @Test
    void lancaExcecaoQuandoUsuarioNaoExiste() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> service.identify(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    private void givenExistingUser() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bodyProfileRepository.findByCustomerId(userId)).thenReturn(Optional.of(profile));
    }

    private void withAnswers(String... answers) {
        ReflectionTestUtils.setField(profile, "stylePreference", Arrays.copyOf(answers, answers.length));
    }
}
