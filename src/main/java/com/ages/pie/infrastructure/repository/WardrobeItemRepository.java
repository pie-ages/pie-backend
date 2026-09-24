package com.ages.pie.infrastructure.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.WardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, UUID> {

    List<WardrobeItem> findByCustomerId(UUID customerId);

    List<WardrobeItem> findByIdInAndCustomerId(Collection<UUID> ids, UUID customerId);

    Optional<WardrobeItem> findByIdAndCustomerId(UUID id, UUID customerId);
}
