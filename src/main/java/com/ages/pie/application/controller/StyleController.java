package com.ages.pie.application.controller;

import java.util.List;

import com.ages.pie.application.dto.style.StyleQuestionResponseDTO;
import com.ages.pie.application.dto.style.StyleResultResponseDTO;
import com.ages.pie.application.dto.style.SubmitStyleAnswersRequestDTO;
import com.ages.pie.application.service.StyleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StyleController {

    private final StyleService styleService;

    public StyleController(StyleService styleService) {
        this.styleService = styleService;
    }

    @GetMapping("/style/questions")
    public ResponseEntity<List<StyleQuestionResponseDTO>> findQuestions() {
        return ResponseEntity.ok(styleService.findQuestions());
    }

    @PostMapping("/users/me/style/answers")
    public ResponseEntity<StyleResultResponseDTO> submitAnswers(
            @Valid @RequestBody SubmitStyleAnswersRequestDTO dto) {
        return ResponseEntity.ok(styleService.calculateAnswers(dto.answers()));
    }
}
