package com.ages.pie.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ages.pie.application.dto.style.StyleAnswerRequestDTO;
import com.ages.pie.application.dto.style.StyleOptionResponseDTO;
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

        List<UUID> ids = questions.stream().map(StyleQuestion::getId).toList();
        Map<UUID, List<StyleOption>> optionsByQuestion = optionRepository
                .findByQuestionIdInOrderByDisplayOrderAsc(ids)
                .stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));
                
        return questions.stream()
                .map(q -> new StyleMapper().toQuestionDTO(q, optionsByQuestion.getOrDefault(q.getId(), List.of())))
                .toList();
    }

    @Transactional
    public StyleResultResponseDTO submitAnswers(UUID userId, List<StyleAnswerRequestDTO> answers) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        List<StyleQuestion> questions = loadActiveQuestions();
        Map<UUID, StyleAnswerRequestDTO> answerByQuestionId = validateAndIndex(questions, answers);
        Map<UUID, StyleOption> optionById = resolveOptions(questions, answerByQuestionId);


        answerRepository.deleteByCustomerId(userId);
        answerRepository.saveAll(buildAnswers(user, questions, answerByQuestionId, optionById));

        List<Style> styles = questions.stream()
                .map(question -> answerByQuestionId.get(question.getId()).optionId())
                .filter(optionId -> optionId != null)
                .map(optionById::get)
                .filter(option -> option != null)
                .map(StyleOption::getStyle)
                .toList();
        List<String> topStyles = styles.isEmpty() ? List.of()
                : StyleScoreCalculator.calculateTopStyles(styles).stream()
                        .map(Style::name)
                        .toList();
        user.updateStyleResult(topStyles);
        userRepository.save(user);

        return new StyleResultResponseDTO(topStyles);
    }

    @Transactional(readOnly = true)
    public StyleResultResponseDTO getStyleResult(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));
        return new StyleResultResponseDTO(List.copyOf(user.getStyleResult()));
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

    private Map<UUID, StyleOption> resolveOptions(
            List<StyleQuestion> questions, Map<UUID, StyleAnswerRequestDTO> answerByQuestionId) {
        Set<UUID> optionIds = answerByQuestionId.values().stream()
                .map(StyleAnswerRequestDTO::optionId)
                .filter(optionId -> optionId != null)
                .collect(Collectors.toSet());
        Map<UUID, StyleOption> optionById = optionIds.isEmpty() ? Map.of()
                : optionRepository.findAllById(optionIds)
                        .stream()
                        .collect(Collectors.toMap(StyleOption::getId, Function.identity()));

        for (StyleQuestion question : questions) {
            StyleAnswerRequestDTO answer = answerByQuestionId.get(question.getId());
            if (answer.answerType() != StyleAnswerType.OPTION) {
                continue;
            }
            UUID optionId = answer.optionId();
            StyleOption option = optionById.get(optionId);
            if (option == null) {
                throw new ResourceNotFoundException("Opção não encontrada: " + optionId);
            }
            if (option.getQuestion() == null || !option.getQuestion().getId().equals(question.getId())) {
                throw new IllegalArgumentException(
                        "Opção não pertence à pergunta: " + optionId);
            }
        }
        return optionById;
    }

    private List<StyleAnswer> buildAnswers(
            User user, List<StyleQuestion> questions,
            Map<UUID, StyleAnswerRequestDTO> answerByQuestionId, Map<UUID, StyleOption> optionById) {
        List<StyleAnswer> toSave = new ArrayList<>();
        for (StyleQuestion question : questions) {
            StyleAnswerRequestDTO answer = answerByQuestionId.get(question.getId());
            StyleOption option = answer.optionId() == null ? null : optionById.get(answer.optionId());
            toSave.add(new StyleAnswer(user, question, option, answer.answerType()));
        }
        return toSave;
    }

}
