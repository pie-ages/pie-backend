package com.ages.pie.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ages.pie.application.dto.style.StyleAnswerRequestDTO;
import com.ages.pie.application.dto.style.StyleQuestionResponseDTO;
import com.ages.pie.application.dto.style.StyleResultResponseDTO;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.domain.entity.StyleAnswer;
import com.ages.pie.domain.entity.StyleOption;
import com.ages.pie.domain.entity.StyleQuestion;
import com.ages.pie.domain.entity.User;
import com.ages.pie.domain.enums.Style;
import com.ages.pie.domain.enums.StyleAnswerType;
import com.ages.pie.infrastructure.repository.StyleAnswerRepository;
import com.ages.pie.infrastructure.repository.StyleOptionRepository;
import com.ages.pie.infrastructure.repository.StyleQuestionRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ages.pie.application.mapper.StyleMapper;

@Service
public class StyleService {

    private final StyleQuestionRepository questionRepository;
    private final StyleOptionRepository optionRepository;
    private final StyleAnswerRepository answerRepository;
    private final UserRepository userRepository;

    public StyleService(StyleQuestionRepository questionRepository,
            StyleOptionRepository optionRepository,
            StyleAnswerRepository answerRepository,
            UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<StyleQuestionResponseDTO> findQuestions() {
        List<StyleQuestion> questions = loadActiveQuestions();
        Map<UUID, List<StyleOption>> optionsByQuestion = loadPairs(questions);
                
        return questions.stream()
            .map(q -> new StyleMapper().toQuestionDTO(q, optionsByQuestion.get(q.getId())))
                .toList();
    }

    @Transactional
    public StyleResultResponseDTO submitAnswers(UUID userId, List<StyleAnswerRequestDTO> answers) {
        User user = findUser(userId);

        List<StyleQuestion> questions = loadActiveQuestions();
        Map<UUID, StyleAnswerRequestDTO> answerByQuestionId = validateAndIndex(questions, answers);
        Map<UUID, List<StyleOption>> pairsByQuestion = loadPairs(questions);
        validateSelectedOptions(questions, answerByQuestionId, pairsByQuestion);


        answerRepository.deleteByCustomerId(userId);
        answerRepository.saveAll(buildAnswers(user, questions, answerByQuestionId, pairsByQuestion));

        List<Style> styles = new ArrayList<>();
        for (StyleQuestion question : questions) {
            StyleAnswerRequestDTO answer = answerByQuestionId.get(question.getId());
            List<StyleOption> pair = pairsByQuestion.get(question.getId());
            if (answer.answerType() == StyleAnswerType.BOTH) {
                pair.stream().map(StyleOption::getStyle).forEach(styles::add);
            } else if (answer.answerType() == StyleAnswerType.OPTION) {
                pair.stream().filter(option -> option.getId().equals(answer.optionId()))
                        .map(StyleOption::getStyle).forEach(styles::add);
            }
        }
        List<String> topStyles = styles.isEmpty() ? List.of()
                : StyleScoreCalculator.calculateTopStyles(styles).stream()
                        .map(Style::name)
                        .toList();
        user.updateStyleResult(topStyles);
        if (user.getStylePreference().isEmpty() && !topStyles.isEmpty()) {
            user.updateStylePreference(topStyles.stream().map(Style::valueOf).toList());
        }
        userRepository.save(user);

        return new StyleResultResponseDTO(topStyles);
    }

    @Transactional(readOnly = true)
    public StyleResultResponseDTO getStyles(UUID userId) {
        return new StyleResultResponseDTO(findUser(userId).getStylePreference().stream().map(Style::name).toList());
    }

    @Transactional
    public StyleResultResponseDTO updateStyles(UUID userId, List<Style> styles) {
        if (styles == null || styles.isEmpty() || styles.stream().anyMatch(style -> style == null)
            || styles.stream().distinct().count() != styles.size()) {
            throw new IllegalArgumentException("Informe estilos válidos e sem repetição");
        }
        User user = findUser(userId);
        user.updateStylePreference(styles);
        userRepository.save(user);
        return new StyleResultResponseDTO(user.getStylePreference().stream().map(Style::name).toList());
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));
    }

    private List<StyleQuestion> loadActiveQuestions() {
        List<StyleQuestion> questions = questionRepository.findByActiveTrueOrderByDisplayOrderAsc();
        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("Questionário de estilo não encontrado");
        }
        return questions;
    }

    private Map<UUID, StyleAnswerRequestDTO> validateAndIndex(
            List<StyleQuestion> questions, List<StyleAnswerRequestDTO> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("Respostas são obrigatórias");
        }
        Set<UUID> expectedQuestionIds = questions.stream()
                .map(StyleQuestion::getId)
                .collect(Collectors.toSet());
        Map<UUID, StyleAnswerRequestDTO> answerByQuestionId = new HashMap<>();
        for (StyleAnswerRequestDTO answer : answers) {
            if (answer == null || answer.questionId() == null) {
                throw new IllegalArgumentException("Pergunta é obrigatória");
            }
            if (!expectedQuestionIds.contains(answer.questionId())) {
                throw new IllegalArgumentException("Pergunta inválida: " + answer.questionId());
            }
            if (answerByQuestionId.put(answer.questionId(), answer) != null) {
                throw new IllegalArgumentException("Pergunta respondida mais de uma vez: " + answer.questionId());
            }
        }
        if (answerByQuestionId.size() != expectedQuestionIds.size()) {
            throw new IllegalArgumentException("Todas as perguntas devem ser respondidas");
        }
        for (StyleAnswerRequestDTO answer : answerByQuestionId.values()) {
            if (answer.answerType() == null) {
                throw new IllegalArgumentException("Tipo de resposta é obrigatório");
            }
            if (answer.answerType() == StyleAnswerType.OPTION && answer.optionId() == null) {
                throw new IllegalArgumentException(
                        "Opção é obrigatória para resposta do tipo OPTION: " + answer.questionId());
            }
            if (answer.answerType() != StyleAnswerType.OPTION && answer.optionId() != null) {
                throw new IllegalArgumentException(
                        "Opção deve ser nula para resposta do tipo " + answer.answerType()
                                + ": " + answer.questionId());
            }
        }
        return answerByQuestionId;
    }

    private Map<UUID, List<StyleOption>> loadPairs(List<StyleQuestion> questions) {
        Map<UUID, List<StyleOption>> optionsByQuestion = optionRepository
                .findByQuestionIdInOrderByDisplayOrderAsc(questions.stream().map(StyleQuestion::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));
        Map<UUID, List<StyleOption>> pairs = new HashMap<>();
        for (StyleQuestion question : questions) {
            List<StyleOption> options = optionsByQuestion.getOrDefault(question.getId(), List.of());
            if (options.size() < 2) {
                throw new IllegalStateException("Pergunta sem duas opções: " + question.getId());
            }
            pairs.put(question.getId(), options.subList(0, 2));
        }
        return pairs;
    }

    private void validateSelectedOptions(List<StyleQuestion> questions,
            Map<UUID, StyleAnswerRequestDTO> answerByQuestionId, Map<UUID, List<StyleOption>> pairsByQuestion) {
        for (StyleQuestion question : questions) {
            StyleAnswerRequestDTO answer = answerByQuestionId.get(question.getId());
            if (answer.answerType() == StyleAnswerType.OPTION && pairsByQuestion.get(question.getId()).stream()
                    .noneMatch(option -> option.getId().equals(answer.optionId()))) {
                throw new IllegalArgumentException("Opção não apresentada na pergunta: " + answer.optionId());
            }
        }
    }

    private List<StyleAnswer> buildAnswers(
            User user, List<StyleQuestion> questions,
            Map<UUID, StyleAnswerRequestDTO> answerByQuestionId, Map<UUID, List<StyleOption>> pairsByQuestion) {
        List<StyleAnswer> toSave = new ArrayList<>();
        for (StyleQuestion question : questions) {
            StyleAnswerRequestDTO answer = answerByQuestionId.get(question.getId());
            StyleOption option = answer.answerType() == StyleAnswerType.OPTION
                ? pairsByQuestion.get(question.getId()).stream()
                    .filter(candidate -> candidate.getId().equals(answer.optionId())).findFirst().orElseThrow()
                : null;
            toSave.add(new StyleAnswer(user, question, option, answer.answerType()));
        }
        return toSave;
    }

}
