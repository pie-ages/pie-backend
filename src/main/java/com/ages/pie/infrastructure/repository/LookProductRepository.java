package com.ages.pie.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.LookProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LookProductRepository extends JpaRepository<LookProduct, UUID> {

    Optional<LookProduct> findByLookIdAndProductId(UUID lookId, UUID productId);

    boolean existsByLookIdAndProductId(UUID lookId, UUID productId);

    void deleteByLookId(UUID lookId);

    @Query("select coalesce(max(l.position), -1) from LookProduct l where l.look.id = :lookId")
    int findMaxPositionByLookId(@Param("lookId") UUID lookId);
}
