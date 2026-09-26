package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.style.StyleOptionResponseDTO;
import com.ages.pie.application.dto.style.StyleQuestionResponseDTO;
import com.ages.pie.domain.entity.StyleOption;
import com.ages.pie.domain.entity.StyleQuestion;
import org.springframework.stereotype.Component;
import java.util.List;


@Component 
public class StyleMapper {

    public StyleQuestionResponseDTO toQuestionDTO(StyleQuestion question, List<StyleOption> options) {
        List<StyleOptionResponseDTO> optionDTOs = options.stream()
                .map(this::toOptionDTO)
                .toList();

        return new StyleQuestionResponseDTO(
                question.getId(),
                question.getText(),
                question.getDisplayOrder(),
                optionDTOs
        );
    }

    public StyleOptionResponseDTO toOptionDTO(StyleOption option) {
        return new StyleOptionResponseDTO(
                option.getId(),
                option.getLabel(),
                option.getImageUrl(),
                option.getDisplayOrder()
        );
    }
}