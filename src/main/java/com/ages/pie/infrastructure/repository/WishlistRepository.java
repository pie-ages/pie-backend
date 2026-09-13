package com.ages.pie.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    Optional<Wishlist> findByCustomerId(UUID customerId);
}
