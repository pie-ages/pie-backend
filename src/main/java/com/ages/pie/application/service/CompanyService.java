package com.ages.pie.application.service;

import java.util.List;
import java.util.UUID;

import com.ages.pie.application.dto.CompanyRequestDTO;
import com.ages.pie.application.dto.CompanyResponseDTO;
import com.ages.pie.application.dto.CompanyUpdateDTO;
import com.ages.pie.application.exception.DuplicateResourceException;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.mapper.CompanyMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional
    public CompanyResponseDTO criar(CompanyRequestDTO requestDTO) {
        String passwordHash = criptografarSenha(requestDTO.password());

        if (companyRepository.existsByCnpj(requestDTO.cnpj())) {
            throw new DuplicateResourceException("Já existe uma empresa com esse CNPJ");
        }
        if (companyRepository.existsByEmail(requestDTO.email())) {
            throw new DuplicateResourceException("Já existe uma empresa com esse email");
        }

        Company salvo = companyRepository.save(companyMapper.toEntity(requestDTO, passwordHash));
        return companyMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> listar() {
        return companyRepository.findAllByActiveTrue().stream()
            .map(companyMapper::toResponseDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDTO buscarPorId(UUID id) {
        return companyMapper.toResponseDTO(buscarEntidade(id));
    }

    @Transactional
    public CompanyResponseDTO atualizar(UUID id, CompanyUpdateDTO updateDTO) {
        Company company = buscarEntidade(id);

        if (companyRepository.existsByEmailAndIdNot(updateDTO.email(), id)) {
            throw new DuplicateResourceException("Já existe uma empresa com esse email");
        }

        company.atualizarDados(
            updateDTO.name(),
            updateDTO.socialReason(),
            updateDTO.responsiblePerson(),
            updateDTO.email(),
            updateDTO.website(),
            updateDTO.photoUrl()
        );
        return companyMapper.toResponseDTO(companyRepository.save(company));
    }

    @Transactional
    public void desativar(UUID id) {
        Company company = buscarEntidade(id);
        company.desativar();
        companyRepository.save(company);
    }

    private Company buscarEntidade(UUID id) {
        return companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
    }

    private String criptografarSenha(String senhaPura) {
        if (senhaPura == null || senhaPura.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        // TODO: injetar um PasswordEncoder do Spring Security em vez de simular a criptografia
        return "hash(" + senhaPura + ")";
    }
}
