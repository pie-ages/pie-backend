package com.ages.pie.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.Look;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LookRepository extends JpaRepository<Look, UUID> {

    List<Look> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Optional<Look> findByIdAndCustomerId(UUID id, UUID customerId);
}
