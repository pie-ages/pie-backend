package com.ages.pie.application.controller;

import java.util.List;

import com.ages.pie.application.dto.style.StyleQuestionResponseDTO;
import com.ages.pie.application.dto.style.StyleResultResponseDTO;
import com.ages.pie.application.dto.style.SubmitStyleAnswersRequestDTO;
import com.ages.pie.application.service.StyleService;
import com.ages.pie.infrastructure.security.AuthenticatedUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StyleController {

    private final StyleService styleService;
    private final AuthenticatedUserProvider authenticatedUser;

    public StyleController(StyleService styleService,
            AuthenticatedUserProvider authenticatedUser) {
        this.styleService = styleService;
        this.authenticatedUser = authenticatedUser;
    }

    @GetMapping("/style/questions")
    public ResponseEntity<List<StyleQuestionResponseDTO>> findQuestions() {
        return ResponseEntity.ok(styleService.findQuestions());
    }

    @PostMapping("/users/me/style/answers")
    public ResponseEntity<StyleResultResponseDTO> submitAnswers(
            @Valid @RequestBody SubmitStyleAnswersRequestDTO dto) {
        return ResponseEntity.ok(styleService.submitAnswers(authenticatedUser.id(), dto.answers()));
    }
}
