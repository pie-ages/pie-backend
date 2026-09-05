package com.ages.pie.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import com.ages.pie.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    List<Company> findAllByActiveTrue();

    boolean existsByCnpj(String cnpj);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
