package com.ages.pie.infrastructure.repository;

import java.util.UUID;

import com.ages.pie.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
