package com.ages.pie.application.service;

import com.ages.pie.domain.entity.BodyProfile;
import com.ages.pie.domain.entity.StyleAnswer;
import com.ages.pie.domain.entity.StyleOption;
import com.ages.pie.domain.entity.StyleQuestion;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.Style;
import com.ages.pie.domain.enums.StyleAnswerType;
import com.ages.pie.infrastructure.repository.BodyProfileRepository;
import com.ages.pie.infrastructure.repository.StyleAnswerRepository;
import com.ages.pie.infrastructure.repository.StyleOptionRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StyleIdentificationFromAnswersTest {

    @Test
    void identifiesSinglePersistedQuestionnaireAnswer() {
        UUID userId = UUID.randomUUID();
        User user = new User("Ana", "ana@pie.com", "hash");
        ReflectionTestUtils.setField(user, "id", userId);
        BodyProfile profile = new BodyProfile(user);
        StyleQuestion question = new StyleQuestion("Qual look?", 1);
        ReflectionTestUtils.setField(question, "id", UUID.randomUUID());
        StyleOption option = new StyleOption(question, "Alfaiataria", Style.ELEGANTE, 1);
        StyleAnswer answer = new StyleAnswer(user, question, option, StyleAnswerType.OPTION);
        BodyProfileRepository profiles = mock(BodyProfileRepository.class);
        StyleAnswerRepository answers = mock(StyleAnswerRepository.class);
        StyleOptionRepository options = mock(StyleOptionRepository.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(profiles.findByCustomerId(userId)).thenReturn(Optional.of(profile));
        when(answers.findByCustomerId(userId)).thenReturn(List.of(answer));

        StyleIdentificationService service = new StyleIdentificationService(profiles, users, answers, options);
        assertThat(service.identify(userId).styles()).containsExactly("ELEGANTE");
        assertThat(profile.getIdentifiedStyle()).isEqualTo("ELEGANTE");
        verify(profiles).save(profile);
    }

    @Test
    void bothCountsTwoOptionsAndNoneCountsNone() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        StyleQuestion question = question();
        StyleQuestion ignoredQuestion = question();
        StyleAnswer both = new StyleAnswer(user, question, null, StyleAnswerType.BOTH);
        StyleAnswer none = new StyleAnswer(user, ignoredQuestion, null, StyleAnswerType.NONE);
        BodyProfile profile = new BodyProfile(user);
        profile.setFavoriteColors(new String[] { "#E2725B" });
        BodyProfileRepository profiles = mock(BodyProfileRepository.class);
        StyleAnswerRepository answers = mock(StyleAnswerRepository.class);
        StyleOptionRepository options = mock(StyleOptionRepository.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(profiles.findByCustomerId(userId)).thenReturn(Optional.of(profile));
        when(answers.findByCustomerId(userId)).thenReturn(List.of(both, none));
        when(options.findByQuestionIdOrderByDisplayOrderAsc(question.getId())).thenReturn(List.of(
                new StyleOption(question, "Casual", Style.CASUAL, 1),
                new StyleOption(question, "Boho", Style.BOHO, 2)));

        assertThat(new StyleIdentificationService(profiles, users, answers, options)
            .identify(userId).styles()).containsExactly("CASUAL");
        assertThat(profile.getIdentifiedStyle()).isEqualTo("CASUAL");
        verify(options, never()).findByQuestionIdOrderByDisplayOrderAsc(ignoredQuestion.getId());
    }

    @Test
    void colorsDoNotChangeAnswerOrManualPreference() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        user.updateStylePreference(List.of(Style.ROMANTICO));
        BodyProfile profile = new BodyProfile(user);
        profile.setFavoriteColors(new String[] { "#000000", "#FFFFFF", "#808080" });
        StyleQuestion question = question();
        StyleAnswer answer = new StyleAnswer(user, question,
                new StyleOption(question, "Elegante", Style.ELEGANTE, 1), StyleAnswerType.OPTION);
        BodyProfileRepository profiles = mock(BodyProfileRepository.class);
        StyleAnswerRepository answers = mock(StyleAnswerRepository.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(profiles.findByCustomerId(userId)).thenReturn(Optional.of(profile));
        when(answers.findByCustomerId(userId)).thenReturn(List.of(answer));

        List<String> result = new StyleIdentificationService(profiles, users, answers,
                mock(StyleOptionRepository.class)).identify(userId).styles();

        assertThat(result).containsExactly("ELEGANTE");
        assertThat(user.getStylePreference()).containsExactly(Style.ROMANTICO);
        assertThat(Style.valueOf(result.getFirst())).isEqualTo(Style.ELEGANTE);
    }

    @Test
    void favoriteColorAloneDoesNotIdentifyStyle() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        BodyProfile profile = new BodyProfile(user);
        profile.setFavoriteColors(new String[] { "#2e7d32" });
        BodyProfileRepository profiles = mock(BodyProfileRepository.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(profiles.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        assertThat(new StyleIdentificationService(profiles, users,
                mock(StyleAnswerRepository.class), mock(StyleOptionRepository.class))
            .identify(userId).styles()).isEmpty();
        verify(profiles, never()).save(any());
    }

    @Test
    void noDataDoesNotCreateProfileOrErasePreviousResult() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        BodyProfileRepository profiles = mock(BodyProfileRepository.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(profiles.findByCustomerId(userId)).thenReturn(Optional.empty());
        StyleIdentificationService service = new StyleIdentificationService(profiles, users,
                mock(StyleAnswerRepository.class), mock(StyleOptionRepository.class));

        assertThat(service.identify(userId).styles()).isEmpty();
        verify(profiles, never()).save(any());
    }

    @Test
    void noAnswersPreservesIdentifiedStyleEvenWithFavoriteColor() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        BodyProfile profile = new BodyProfile(user);
        profile.setIdentifiedStyle("ROMANTICO");
        profile.setFavoriteColors(new String[] { "#2E7D32" });
        BodyProfileRepository profiles = mock(BodyProfileRepository.class);
        UserRepository users = mock(UserRepository.class);
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(profiles.findByCustomerId(userId)).thenReturn(Optional.of(profile));

        assertThat(new StyleIdentificationService(profiles, users,
                mock(StyleAnswerRepository.class), mock(StyleOptionRepository.class))
                .identify(userId).styles()).isEmpty();
        assertThat(profile.getIdentifiedStyle()).isEqualTo("ROMANTICO");
        verify(profiles, never()).save(any());
    }

    @Test
    void missingUserThrowsNotFound() {
        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> new StyleIdentificationService(mock(BodyProfileRepository.class),
                mock(UserRepository.class), mock(StyleAnswerRepository.class),
                mock(StyleOptionRepository.class)).identify(userId))
                .isInstanceOf(com.ages.pie.application.exception.ResourceNotFoundException.class);
    }

    private User user(UUID userId) {
        User user = new User("Ana", "ana@pie.com", "hash");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private StyleQuestion question() {
        StyleQuestion question = new StyleQuestion("Qual look?", 1);
        ReflectionTestUtils.setField(question, "id", UUID.randomUUID());
        return question;
    }
}