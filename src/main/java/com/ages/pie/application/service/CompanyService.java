package com.ages.pie.application.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.ages.pie.application.dto.company.CompanyRequestDTO;
import com.ages.pie.application.dto.company.CompanyResponseDTO;
import com.ages.pie.application.dto.company.CompanyUpdateDTO;
import com.ages.pie.application.exception.BusinessException;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.mapper.CompanyMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.infrastructure.auth.SupabaseAuthClient;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final SupabaseAuthClient supabaseAuthClient;

    public CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper,
                          SupabaseAuthClient supabaseAuthClient) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.supabaseAuthClient = supabaseAuthClient;
    }

    @Transactional
    public CompanyResponseDTO create(CompanyRequestDTO dto) {
        if (companyRepository.existsByCnpj(dto.cnpj())) {
            throw new BusinessException("CNPJ já cadastrado: " + dto.cnpj());
        }
        if (companyRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email já cadastrado: " + dto.email());
        }

        UUID supabaseUserId = supabaseAuthClient.createUser(dto.email(), dto.password());

        try {
            Company company = companyMapper.toEntity(dto, supabaseUserId);
            return companyMapper.toResponseDTO(companyRepository.saveAndFlush(company));
        } catch (RuntimeException e) {
            rollbackSupabaseUser(supabaseUserId, dto.email());
            throw e;
        }
    }

    private void rollbackSupabaseUser(UUID supabaseUserId, String email) {
        try {
            supabaseAuthClient.deleteUser(supabaseUserId);
        } catch (RuntimeException e) {
            logger.error("Usuário {} ficou órfão no Supabase Auth após falha ao cadastrar {}",
                    supabaseUserId, email, e);
        }
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
            throw new BusinessException("Email já cadastrado: " + dto.email());
        }

        boolean emailChanged = !Objects.equals(company.getEmail(), dto.email());
        company.update(dto.name(), dto.socialReason(), dto.responsiblePerson(),
                dto.email(), dto.website(), dto.photoUrl());
        CompanyResponseDTO response = companyMapper.toResponseDTO(companyRepository.saveAndFlush(company));

        if (emailChanged && company.getSupabaseUserId() != null) {
            supabaseAuthClient.updateEmail(company.getSupabaseUserId(), dto.email());
        }

        return response;
    }

    @Transactional
    public void deactivate(UUID id) {
        Company company = findEntity(id);
        company.deactivate();
        companyRepository.save(company);
    }

    private Company findEntity(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));
    }
}
