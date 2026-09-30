package com.ages.pie.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.LookWardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LookWardrobeItemRepository extends JpaRepository<LookWardrobeItem, UUID> {

    Optional<LookWardrobeItem> findByLookIdAndWardrobeItemId(UUID lookId, UUID wardrobeItemId);

    boolean existsByLookIdAndWardrobeItemId(UUID lookId, UUID wardrobeItemId);

    void deleteByLookId(UUID lookId);
}
