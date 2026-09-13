package com.ages.pie.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

    Optional<WishlistItem> findByWishlistIdAndProductId(UUID wishlistId, UUID productId);

    boolean existsByWishlistIdAndProductId(UUID wishlistId, UUID productId);
}
