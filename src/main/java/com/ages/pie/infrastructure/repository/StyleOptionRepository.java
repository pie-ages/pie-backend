package com.ages.pie.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import com.ages.pie.domain.entity.StyleOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StyleOptionRepository extends JpaRepository<StyleOption, UUID> {

    List<StyleOption> findByQuestionIdOrderByDisplayOrderAsc(UUID questionId);

    List<StyleOption> findByQuestionIdInOrderByDisplayOrderAsc(List<UUID> questionIds);
}
