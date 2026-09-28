package com.ages.pie.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import com.ages.pie.domain.entity.StyleQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StyleQuestionRepository extends JpaRepository<StyleQuestion, UUID> {

    List<StyleQuestion> findByActiveTrueOrderByDisplayOrderAsc();
}
