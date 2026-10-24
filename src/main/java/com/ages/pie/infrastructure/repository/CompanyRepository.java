package com.ages.pie.infrastructure.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.domain.entity.Company;
import com.ages.pie.domain.enums.CompanyRole;
import com.ages.pie.domain.enums.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

    List<Company> findAllByActiveTrue();

    List<Company> findAllByStatusAndRoleNot(CompanyStatus status, CompanyRole role);

    List<Company> findAllByRoleNot(CompanyRole role);

    List<Company> findAllByStatus(CompanyStatus status);

    Optional<Company> findBySupabaseUserId(UUID supabaseUserId);

    Optional<Company> findByEmail(String email);

    boolean existsByCnpj(String cnpj);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);
}
