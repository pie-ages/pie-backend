package com.ages.pie.application.service;

import com.ages.pie.application.dto.style.StyleAnswerRequestDTO;
import com.ages.pie.application.dto.style.StyleResultResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.StyleOption;
import com.ages.pie.domain.entity.StyleQuestion;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.Style;
import com.ages.pie.domain.enums.StyleAnswerType;
import com.ages.pie.infrastructure.repository.StyleAnswerRepository;
import com.ages.pie.infrastructure.repository.StyleOptionRepository;
import com.ages.pie.infrastructure.repository.StyleQuestionRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StyleServiceTest {

    @Mock
    private StyleQuestionRepository questionRepository;

    @Mock
    private StyleOptionRepository optionRepository;

    @Mock
    private StyleAnswerRepository answerRepository;

    @Mock
    private UserRepository userRepository;

    private StyleService styleService;

    private UUID userId;
    private User user;
    private StyleQuestion question1;
    private StyleQuestion question2;
    private StyleOption option1;
    private StyleOption option2;

    @BeforeEach
    void setUp() {
        styleService = new StyleService(
                questionRepository, optionRepository, answerRepository, userRepository);

        userId = UUID.randomUUID();
        user = new User("Ana Silva", "ana@email.com", "hash(senha123)");
        ReflectionTestUtils.setField(user, "id", userId);

        question1 = new StyleQuestion("Pergunta 1?", 1);
        question2 = new StyleQuestion("Pergunta 2?", 2);
        ReflectionTestUtils.setField(question1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(question2, "id", UUID.randomUUID());

        option1 = new StyleOption(question1, "Opção 1", Style.CASUAL, 1);
        option2 = new StyleOption(question2, "Opção 2", Style.CASUAL, 1);
        ReflectionTestUtils.setField(option1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(option2, "id", UUID.randomUUID());
    }

    @Test
    void submitAnswers_shouldReturnMajorityStyle_whenAllAnswersAreOption() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findByActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(question1, question2));
        when(optionRepository.findAllById(Set.of(option1.getId(), option2.getId())))
                .thenReturn(List.of(option1, option2));

        List<StyleAnswerRequestDTO> answers = List.of(
                new StyleAnswerRequestDTO(question1.getId(), option1.getId(), StyleAnswerType.OPTION),
                new StyleAnswerRequestDTO(question2.getId(), option2.getId(), StyleAnswerType.OPTION));

        StyleResultResponseDTO result = styleService.submitAnswers(userId, answers);

        assertThat(result.styles()).containsExactly("CASUAL");
        assertThat(user.getStyleResult()).containsExactly("CASUAL");
        verify(answerRepository).deleteByCustomerId(userId);
        verify(answerRepository).saveAll(any());
        verify(userRepository).save(user);
    }

    @Test
    void submitAnswers_shouldIgnoreBothAndNone_whenCalculatingResult() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findByActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(question1, question2));
        when(optionRepository.findAllById(Set.of(option1.getId())))
                .thenReturn(List.of(option1));

        List<StyleAnswerRequestDTO> answers = List.of(
                new StyleAnswerRequestDTO(question1.getId(), option1.getId(), StyleAnswerType.OPTION),
                new StyleAnswerRequestDTO(question2.getId(), null, StyleAnswerType.BOTH));

        StyleResultResponseDTO result = styleService.submitAnswers(userId, answers);

        assertThat(result.styles()).containsExactly("CASUAL");
    }

    @Test
    void submitAnswers_shouldReturnEmptyResult_whenAllAnswersAreNone() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findByActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(question1, question2));

        List<StyleAnswerRequestDTO> answers = List.of(
                new StyleAnswerRequestDTO(question1.getId(), null, StyleAnswerType.NONE),
                new StyleAnswerRequestDTO(question2.getId(), null, StyleAnswerType.BOTH));

        StyleResultResponseDTO result = styleService.submitAnswers(userId, answers);

        assertThat(result.styles()).isEmpty();
        assertThat(user.getStyleResult()).isEmpty();
    }

    @Test
    void submitAnswers_shouldThrowIllegalArgumentException_whenOptionAnswerHasNullOption() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findByActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(question1, question2));

        List<StyleAnswerRequestDTO> answers = List.of(
                new StyleAnswerRequestDTO(question1.getId(), null, StyleAnswerType.OPTION),
                new StyleAnswerRequestDTO(question2.getId(), null, StyleAnswerType.NONE));

        assertThatThrownBy(() -> styleService.submitAnswers(userId, answers))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Opção é obrigatória");
    }

    @Test
    void submitAnswers_shouldThrowIllegalArgumentException_whenBothAnswerHasOption() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findByActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(question1, question2));

        List<StyleAnswerRequestDTO> answers = List.of(
                new StyleAnswerRequestDTO(question1.getId(), null, StyleAnswerType.NONE),
                new StyleAnswerRequestDTO(question2.getId(), option2.getId(), StyleAnswerType.BOTH));

        assertThatThrownBy(() -> styleService.submitAnswers(userId, answers))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Opção deve ser nula");
    }

    @Test
    void submitAnswers_shouldThrowIllegalArgumentException_whenQuestionIsMissing() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(questionRepository.findByActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(question1, question2));

        List<StyleAnswerRequestDTO> answers = List.of(
                new StyleAnswerRequestDTO(question1.getId(), option1.getId(), StyleAnswerType.OPTION));

        assertThatThrownBy(() -> styleService.submitAnswers(userId, answers))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Todas as perguntas devem ser respondidas");
    }

    @Test
    void submitAnswers_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        List<StyleAnswerRequestDTO> answers = List.of(
                new StyleAnswerRequestDTO(question1.getId(), null, StyleAnswerType.NONE),
                new StyleAnswerRequestDTO(question2.getId(), null, StyleAnswerType.NONE));

        assertThatThrownBy(() -> styleService.submitAnswers(userId, answers))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    void getStyleResult_shouldReturnUserStyles() {
        user.updateStyleResult(List.of("CASUAL", "BOHO"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        StyleResultResponseDTO result = styleService.getStyleResult(userId);

        assertThat(result.styles()).containsExactly("CASUAL", "BOHO");
    }

    @Test
    void getStyleResult_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> styleService.getStyleResult(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }
}
