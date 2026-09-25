package com.ages.pie.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.BodyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyProfileRepository extends JpaRepository<BodyProfile, UUID> {

    Optional<BodyProfile> findByCustomerId(UUID customerId);
}
