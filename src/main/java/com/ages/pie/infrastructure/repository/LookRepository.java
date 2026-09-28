package com.ages.pie.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.Look;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LookRepository extends JpaRepository<Look, UUID> {

        @Query("select look from Look look where look.customer.id = :customerId "
            + "order by look.createdAt desc, look.id desc")
        Page<Look> findByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

            List<Look> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Optional<Look> findByIdAndCustomerId(UUID id, UUID customerId);
}
