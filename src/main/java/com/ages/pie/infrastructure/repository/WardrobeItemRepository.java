package com.ages.pie.infrastructure.repository;

import com.ages.pie.domain.entity.WardrobeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface WardrobeItemRepository extends JpaRepository<WardrobeItem, UUID> {

    Optional<WardrobeItem> findByIdAndCustomerId(UUID id, UUID customerId);

    List<WardrobeItem> findAllByCustomerIdOrderByCreatedAtDesc(UUID customerId);

        @Query("select distinct item.category from WardrobeItem item "
            + "where item.customer.id = :customerId order by item.category asc")
        List<String> findCategoriesByCustomerIdOrderByCategoryAsc(@Param("customerId") UUID customerId);

    Page<WardrobeItem> findAllByCustomerIdAndCategoryOrderByCreatedAtDescIdDesc(
            UUID customerId, String category, Pageable pageable);
}