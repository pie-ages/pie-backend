package com.ages.pie.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ages.pie.application.dto.CompanyRequestDTO;
import com.ages.pie.application.dto.CompanyResponseDTO;
import com.ages.pie.application.dto.CompanyUpdateDTO;
import com.ages.pie.application.exception.DuplicateResourceException;
import com.ages.pie.application.exception.ResourceNotFoundException;
import com.ages.pie.application.mapper.CompanyMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    private CompanyRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new CompanyRequestDTO("Loja X", "12345678000199", "Loja X LTDA",
            "Maria", "contato@lojax.com", "senha123", "https://lojax.com", null);
    }

    @Test
    void criaEmpresaHasheandoASenha() {
        Company entidade = company();
        when(companyRepository.existsByCnpj(requestDTO.cnpj())).thenReturn(false);
        when(companyRepository.existsByEmail(requestDTO.email())).thenReturn(false);
        when(companyMapper.toEntity(eq(requestDTO), any())).thenReturn(entidade);
        when(companyRepository.save(entidade)).thenReturn(entidade);
        when(companyMapper.toResponseDTO(entidade)).thenReturn(response(entidade));

        assertThat(companyService.criar(requestDTO)).isNotNull();

        ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
        verify(companyMapper).toEntity(eq(requestDTO), hash.capture());
        assertThat(hash.getValue()).isNotBlank().isNotEqualTo("senha123");
    }

    @Test
    void criaEmpresaRejeitaSenhaEmBranco() {
        CompanyRequestDTO semSenha = new CompanyRequestDTO("Loja", "123", "Razao",
            "Maria", "a@b.com", "  ", null, null);

        assertThatThrownBy(() -> companyService.criar(semSenha))
            .isInstanceOf(IllegalArgumentException.class);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void criaEmpresaRejeitaCnpjDuplicado() {
        when(companyRepository.existsByCnpj(requestDTO.cnpj())).thenReturn(true);

        assertThatThrownBy(() -> companyService.criar(requestDTO))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Já existe uma empresa com esse CNPJ");
        verify(companyRepository, never()).save(any());
    }

    @Test
    void criaEmpresaRejeitaEmailDuplicado() {
        when(companyRepository.existsByCnpj(requestDTO.cnpj())).thenReturn(false);
        when(companyRepository.existsByEmail(requestDTO.email())).thenReturn(true);

        assertThatThrownBy(() -> companyService.criar(requestDTO))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessage("Já existe uma empresa com esse email");
        verify(companyRepository, never()).save(any());
    }

    @Test
    void listaSomenteAtivas() {
        Company entidade = company();
        when(companyRepository.findAllByActiveTrue()).thenReturn(List.of(entidade));
        when(companyMapper.toResponseDTO(entidade)).thenReturn(response(entidade));

        assertThat(companyService.listar()).hasSize(1);
        verify(companyRepository).findAllByActiveTrue();
    }

    @Test
    void buscaPorIdInexistente() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.buscarPorId(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Empresa não encontrada");
    }

    @Test
    void buscaPorIdExistente() {
        Company entidade = company();
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.of(entidade));
        when(companyMapper.toResponseDTO(entidade)).thenReturn(response(entidade));

        assertThat(companyService.buscarPorId(id)).isNotNull();
    }

    @Test
    void atualizaEmpresaInexistente() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.atualizar(id, updateDTO()))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void atualizaRejeitaEmailDeOutraEmpresa() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.of(company()));
        when(companyRepository.existsByEmailAndIdNot("novo@empresa.com", id)).thenReturn(true);

        assertThatThrownBy(() -> companyService.atualizar(id, updateDTO()))
            .isInstanceOf(DuplicateResourceException.class);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void atualizaEmpresa() {
        Company entidade = company();
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.of(entidade));
        when(companyRepository.existsByEmailAndIdNot("novo@empresa.com", id)).thenReturn(false);
        when(companyRepository.save(entidade)).thenReturn(entidade);
        when(companyMapper.toResponseDTO(entidade)).thenReturn(response(entidade));

        companyService.atualizar(id, updateDTO());

        assertThat(entidade.getName()).isEqualTo("Loja Nova");
        assertThat(entidade.getEmail()).isEqualTo("novo@empresa.com");
        verify(companyRepository).save(entidade);
    }

    @Test
    void desativaEmpresaInexistente() {
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.desativar(id))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void desativaEmpresa() {
        Company entidade = company();
        UUID id = UUID.randomUUID();
        when(companyRepository.findById(id)).thenReturn(Optional.of(entidade));

        companyService.desativar(id);

        assertThat(entidade.isActive()).isFalse();
        verify(companyRepository).save(entidade);
    }

    private Company company() {
        return new Company("Loja X", "12345678000199", "Loja X LTDA", "Maria",
            "contato@lojax.com", "hash", null, null);
    }

    private CompanyUpdateDTO updateDTO() {
        return new CompanyUpdateDTO("Loja Nova", "Loja Nova LTDA", "Joao",
            "novo@empresa.com", null, null);
    }

    private CompanyResponseDTO response(Company c) {
        return new CompanyResponseDTO(UUID.randomUUID(), c.getName(), c.getCnpj(),
            c.getSocialReason(), c.getResponsiblePerson(), c.getEmail(), c.getWebsite(),
            c.isActive(), c.getPhotoUrl(), null, null);
    }
}
