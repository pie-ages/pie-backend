package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.CompanyRequestDTO;
import com.ages.pie.application.dto.CompanyResponseDTO;
import com.ages.pie.domain.entity.Company;
import org.springframework.stereotype.Component;

@Component
public class CompanyMapper {

    public Company toEntity(CompanyRequestDTO dto, String passwordHash) {
        return new Company(
            dto.name(),
            dto.cnpj(),
            dto.socialReason(),
            dto.responsiblePerson(),
            dto.email(),
            passwordHash,
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
            company.getCreatedAt(),
            company.getUpdatedAt()
        );
    }
}
