package com.ages.pie.application.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.style.StyleQuestionResponseDTO;
import com.ages.pie.application.dto.style.StyleOptionResponseDTO;
import com.ages.pie.application.dto.style.StyleQuizResponseDTO;
import com.ages.pie.application.dto.style.StyleResultResponseDTO;
import com.ages.pie.application.dto.style.SubmitStyleAnswersRequestDTO;
import com.ages.pie.application.dto.style.UpdateStylesRequestDTO;
import com.ages.pie.application.dto.user.UserStyleResponseDTO;
import com.ages.pie.application.dto.user.UserStyleUpdateDTO;
import com.ages.pie.application.service.StyleService;
import com.ages.pie.application.service.UserStyleService;
import com.ages.pie.domain.enums.Style;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class StyleController {

    private final StyleService styleService;
    private final UserStyleService userStyleService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public StyleController(StyleService styleService, UserStyleService userStyleService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.styleService = styleService;
        this.userStyleService = userStyleService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @GetMapping("/style/questions")
    public ResponseEntity<StyleQuizResponseDTO> findQuestions() {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        return ResponseEntity.ok(new StyleQuizResponseDTO(styleService.findQuestions().stream()
                .map(question -> new StyleQuestionResponseDTO(question.id(), question.question(),
                        question.order(), question.options().stream()
                                .map(option -> new StyleOptionResponseDTO(option.id(), option.label(),
                            option.imageUrl() == null || URI.create(option.imageUrl()).isAbsolute()
                                ? option.imageUrl() : baseUrl + option.imageUrl(),
                                        option.style(), option.displayOrder()))
                                .toList()))
                .toList()));
    }

    @GetMapping("/styles")
    public ResponseEntity<List<Style>> findStyles() {
        return ResponseEntity.ok(List.of(Style.values()));
    }

    @PostMapping({"/users/me/style/answers", "/users/{userId}/style/answers"})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StyleResultResponseDTO> submitAnswers(
            @PathVariable(required = false) UUID userId,
            @Valid @RequestBody SubmitStyleAnswersRequestDTO dto) {
        UUID authenticatedId = authenticatedUserProvider.id();
        if (userId != null && !userId.equals(authenticatedId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não é possível alterar o estilo de outro usuário");
        }
        return ResponseEntity.ok(styleService.submitAnswers(authenticatedId, dto.answers()));
    }

    @PutMapping("/users/me/style/answers")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StyleResultResponseDTO> updateMyAnswers(
            @Valid @RequestBody SubmitStyleAnswersRequestDTO dto) {
        return ResponseEntity.ok(styleService.submitAnswers(authenticatedUserProvider.id(), dto.answers()));
    }

    @GetMapping("/users/me/style")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StyleResultResponseDTO> getStyles() {
        return ResponseEntity.ok(styleService.getStyles(authenticatedUserProvider.id()));
    }

    @PutMapping("/users/me/style")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<StyleResultResponseDTO> updateStyles(@Valid @RequestBody UpdateStylesRequestDTO dto) {
        return ResponseEntity.ok(styleService.updateStyles(authenticatedUserProvider.id(), dto.styles()));
    }

    @GetMapping("/users/me/product-styles")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserStyleResponseDTO> getMyProductStyles() {
        return ResponseEntity.ok(userStyleService.getMyStyle(authenticatedUserProvider.id()));
    }

    @PutMapping("/users/me/product-styles")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserStyleResponseDTO> updateMyProductStyles(
            @Valid @RequestBody UserStyleUpdateDTO dto) {
        return ResponseEntity.ok(userStyleService.updateMyStyle(authenticatedUserProvider.id(), dto.styles()));
    }
}
