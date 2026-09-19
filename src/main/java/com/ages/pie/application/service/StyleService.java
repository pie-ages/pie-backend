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
import com.ages.pie.infrastructure.repository.StyleAnswerRepository;
import com.ages.pie.infrastructure.repository.StyleOptionRepository;
import com.ages.pie.infrastructure.repository.StyleQuestionRepository;
import com.ages.pie.infrastructure.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .map(question -> toQuestionDTO(question,
                        optionsByQuestion.getOrDefault(question.getId(), List.of())))
                .toList();
    }

    @Transactional
    public StyleResultResponseDTO submitAnswers(UUID userId, List<StyleAnswerRequestDTO> answers) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));

        List<StyleQuestion> questions = loadActiveQuestions();
        Map<UUID, StyleAnswerRequestDTO> answerByQuestionId = validateAndIndex(questions, answers);
        List<StyleOption> options = resolveOptionsInQuestionOrder(questions, answerByQuestionId);


        answerRepository.deleteByCustomerId(userId);
        answerRepository.saveAll(buildAnswers(user, questions, options));

        List<Style> styles = options.stream().map(StyleOption::getStyle).toList();
        Style winner = StyleScoreCalculator.calculate(styles);
        user.updateStyleResult(winner.name());
        userRepository.save(user);

        return new StyleResultResponseDTO(winner.name());
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
        return answerByQuestionId;
    }

    private List<StyleOption> resolveOptionsInQuestionOrder(
            List<StyleQuestion> questions, Map<UUID, StyleAnswerRequestDTO> answerByQuestionId) {
        Set<UUID> optionIds = answerByQuestionId.values().stream()
                .map(StyleAnswerRequestDTO::optionId)
                .collect(Collectors.toSet());
        Map<UUID, StyleOption> optionById = optionRepository.findAllById(optionIds)
                .stream()
                .collect(Collectors.toMap(StyleOption::getId, Function.identity()));

        List<StyleOption> options = new ArrayList<>();
        for (StyleQuestion question : questions) {
            UUID optionId = answerByQuestionId.get(question.getId()).optionId();
            StyleOption option = optionById.get(optionId);
            if (option == null) {
                throw new ResourceNotFoundException("Opção não encontrada: " + optionId);
            }
            if (!option.getQuestion().getId().equals(question.getId())) {
                throw new IllegalArgumentException(
                        "Opção não pertence à pergunta: " + optionId);
            }
            options.add(option);
        }
        return options;
    }

    private List<StyleAnswer> buildAnswers(
            User user, List<StyleQuestion> questions, List<StyleOption> options) {
        List<StyleAnswer> toSave = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            toSave.add(new StyleAnswer(user, questions.get(i), options.get(i)));
        }
        return toSave;
    }

    private StyleQuestionResponseDTO toQuestionDTO(StyleQuestion question, List<StyleOption> options) {
        List<StyleOptionResponseDTO> optionDTOs = options.stream()
                .map(option -> new StyleOptionResponseDTO(
                        option.getId(),
                        option.getLabel(),
                        option.getImageUrl(),
                        option.getDisplayOrder()))
                .toList();
        return new StyleQuestionResponseDTO(
                question.getId(),
                question.getText(),
                question.getDisplayOrder(),
                optionDTOs);
    }
}
