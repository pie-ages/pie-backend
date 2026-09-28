package com.ages.pie.infrastructure.repository;

import java.util.UUID;

import com.ages.pie.domain.entity.StyleAnswer;
import com.ages.pie.domain.entity.StyleAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StyleAnswerRepository extends JpaRepository<StyleAnswer, StyleAnswerId> {

    void deleteByCustomerId(UUID customerId);
}
