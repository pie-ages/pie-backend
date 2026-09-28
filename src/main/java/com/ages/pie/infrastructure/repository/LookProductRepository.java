package com.ages.pie.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.LookProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LookProductRepository extends JpaRepository<LookProduct, UUID> {

    Optional<LookProduct> findByLookIdAndProductId(UUID lookId, UUID productId);

    boolean existsByLookIdAndProductId(UUID lookId, UUID productId);

    void deleteByLookId(UUID lookId);
}
