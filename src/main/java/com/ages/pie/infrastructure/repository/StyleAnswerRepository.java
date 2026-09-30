package com.ages.pie.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import com.ages.pie.domain.entity.StyleAnswer;
import com.ages.pie.domain.entity.StyleAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StyleAnswerRepository extends JpaRepository<StyleAnswer, StyleAnswerId> {

    List<StyleAnswer> findByCustomerId(UUID customerId);

    void deleteByCustomerId(UUID customerId);
}
