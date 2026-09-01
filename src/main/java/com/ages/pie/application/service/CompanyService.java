package com.ages.pie.application.service;

import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.company.CompanyRequestDTO;
import com.ages.pie.application.dto.company.CompanyResponseDTO;
import com.ages.pie.application.dto.company.CompanyUpdateDTO;
import com.ages.pie.application.mapper.CompanyMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional
    public CompanyResponseDTO create(CompanyRequestDTO dto) {
        if (companyRepository.existsByCnpj(dto.cnpj())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CNPJ já cadastrado: " + dto.cnpj());
        }
        if (companyRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado: " + dto.email());
        }

        Company company;
        try {
            company = companyMapper.toEntity(dto, hashPassword(dto.password()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return companyMapper.toResponseDTO(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> findAll() {
        return companyRepository.findAllByActiveTrue()
                .stream()
                .map(companyMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDTO findById(UUID id) {
        return companyMapper.toResponseDTO(findEntity(id));
    }

    @Transactional
    public CompanyResponseDTO update(UUID id, CompanyUpdateDTO dto) {
        Company company = findEntity(id);

        if (companyRepository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado: " + dto.email());
        }

        try {
            company.update(dto.name(), dto.socialReason(), dto.responsiblePerson(),
                    dto.email(), dto.website(), dto.photoUrl());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return companyMapper.toResponseDTO(companyRepository.save(company));
    }

    @Transactional
    public void deactivate(UUID id) {
        Company company = findEntity(id);
        company.deactivate();
        companyRepository.save(company);
    }

    private Company findEntity(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada: " + id));
    }

    private String hashPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha é obrigatória");
        }
        return "hash(" + password + ")";
    }
}
