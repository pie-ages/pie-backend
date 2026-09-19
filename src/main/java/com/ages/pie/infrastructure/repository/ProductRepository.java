package com.ages.pie.infrastructure.repository;

import com.ages.pie.domain.entity.Product;
import com.ages.pie.domain.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query(value = """
            SELECT p FROM Product p
            JOIN FETCH p.company
            WHERE p.active = true
              AND p.status = com.ages.pie.domain.enums.ProductStatus.PUBLISHED
              AND (:search IS NULL
                OR cast(function('unaccent', lower(p.name)) as string) LIKE cast(function('unaccent', lower(concat('%', cast(:search as string), '%'))) as string)
                OR cast(function('unaccent', lower(p.description)) as string) LIKE cast(function('unaccent', lower(concat('%', cast(:search as string), '%'))) as string))
            """,
            countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE p.active = true
              AND p.status = com.ages.pie.domain.enums.ProductStatus.PUBLISHED
              AND (:search IS NULL
                OR cast(function('unaccent', lower(p.name)) as string) LIKE cast(function('unaccent', lower(concat('%', cast(:search as string), '%'))) as string)
                OR cast(function('unaccent', lower(p.description)) as string) LIKE cast(function('unaccent', lower(concat('%', cast(:search as string), '%'))) as string))
            """)
    Page<Product> findCatalog(@Param("search") String search, Pageable pageable);

    @Query(value = """
            SELECT p FROM Product p
            JOIN FETCH p.company
            WHERE p.company.id = :companyId
              AND p.active = true
              AND (:status IS NULL OR p.status = :status)
              AND (:search IS NULL
                OR cast(function('unaccent', lower(p.name)) as string) LIKE cast(function('unaccent', lower(concat('%', cast(:search as string), '%'))) as string)
                OR cast(function('unaccent', lower(p.description)) as string) LIKE cast(function('unaccent', lower(concat('%', cast(:search as string), '%'))) as string))
            """,
            countQuery = """
            SELECT COUNT(p) FROM Product p
            WHERE p.company.id = :companyId
              AND p.active = true
              AND (:status IS NULL OR p.status = :status)
              AND (:search IS NULL
                OR cast(function('unaccent', lower(p.name)) as string) LIKE cast(function('unaccent', lower(concat('%', cast(:search as string), '%'))) as string)
                OR cast(function('unaccent', lower(p.description)) as string) LIKE cast(function('unaccent', lower(concat('%', cast(:search as string), '%'))) as string))
            """)
    Page<Product> findByCompanyFiltered(
            @Param("companyId") UUID companyId,
            @Param("status") ProductStatus status,
            @Param("search") String search,
            Pageable pageable);
}
