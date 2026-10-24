package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.company.CompanyRequestDTO;
import com.ages.pie.application.dto.company.CompanyResponseDTO;
import com.ages.pie.domain.entity.Company;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CompanyMapper {

    public Company toEntity(CompanyRequestDTO dto, UUID supabaseUserId) {
        return new Company(
            null,
            dto.name(),
            dto.cnpj(),
            dto.socialReason(),
            dto.responsiblePerson(),
            dto.email(),
            supabaseUserId,
            dto.website(),
            dto.photoUrl()
        );
    }

    public CompanyResponseDTO toResponseDTO(Company company) {
        return new CompanyResponseDTO(
            company.getId(),
            company.getName(),
            company.getCnpj(),
            company.getSocialReason(),
            company.getResponsiblePerson(),
            company.getEmail(),
            company.getWebsite(),
            company.isActive(),
            company.getPhotoUrl(),
            company.getRole() != null ? company.getRole().name() : null,
            company.getStatus() != null ? company.getStatus().name() : null,
            company.getCreatedAt(),
            company.getUpdatedAt()
        );
    }
}
