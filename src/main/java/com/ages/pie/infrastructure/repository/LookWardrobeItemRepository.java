package com.ages.pie.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.LookWardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LookWardrobeItemRepository extends JpaRepository<LookWardrobeItem, UUID> {

    Optional<LookWardrobeItem> findByLookIdAndWardrobeItemId(UUID lookId, UUID wardrobeItemId);

    boolean existsByLookIdAndWardrobeItemId(UUID lookId, UUID wardrobeItemId);

    void deleteByLookId(UUID lookId);

    @Query("select coalesce(max(l.position), -1) from LookWardrobeItem l where l.look.id = :lookId")
    int findMaxPositionByLookId(@Param("lookId") UUID lookId);
}
