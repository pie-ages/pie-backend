package com.ages.pie.infrastructure.repository;

import com.ages.pie.domain.entity.WardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, UUID> {

    Optional<WardrobeItem> findByIdAndCustomerId(UUID id, UUID customerId);

    List<WardrobeItem> findAllByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}