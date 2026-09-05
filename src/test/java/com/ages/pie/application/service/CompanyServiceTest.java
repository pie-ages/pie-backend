package com.ages.pie.application.service;

import com.ages.pie.application.dto.company.CompanyRequestDTO;
import com.ages.pie.application.dto.company.CompanyResponseDTO;
import com.ages.pie.application.dto.company.CompanyUpdateDTO;
import com.ages.pie.application.mapper.CompanyMapper;
import com.ages.pie.domain.entity.Company;
import com.ages.pie.infrastructure.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    private UUID companyId;
    private Company company;
    private CompanyResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = new Company(companyId, "Loja X", "12345678000199", "Loja X LTDA", "Maria",
                "contato@lojax.com", "hash(senha123)", null, null);
        responseDTO = new CompanyResponseDTO(companyId, "Loja X", "12345678000199", "Loja X LTDA",
                "Maria", "contato@lojax.com", null, true, null, OffsetDateTime.now(), OffsetDateTime.now());
    }

    private CompanyRequestDTO requestDTO() {
        return new CompanyRequestDTO("Loja X", "12345678000199", "Loja X LTDA", "Maria",
                "contato@lojax.com", "senha123", null, null);
    }

    private CompanyUpdateDTO updateDTO() {
        return new CompanyUpdateDTO("Loja Y", "Loja Y LTDA", "Joao", "novo@lojay.com", null, null);
    }

    private static int statusOf(Throwable thrown) {
        assertThat(thrown).isInstanceOf(ResponseStatusException.class);
        return ((ResponseStatusException) thrown).getStatusCode().value();
    }

    @Test
    void create_shouldReturnResponseDTO_whenDataIsValid() {
        CompanyRequestDTO dto = requestDTO();
        when(companyRepository.existsByCnpj(dto.cnpj())).thenReturn(false);
        when(companyRepository.existsByEmail(dto.email())).thenReturn(false);
        when(companyMapper.toEntity(any(), any())).thenReturn(company);
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toResponseDTO(company)).thenReturn(responseDTO);

        CompanyResponseDTO result = companyService.create(dto);

        assertThat(result).isEqualTo(responseDTO);
        verify(companyRepository).save(company);
    }

    @Test
    void create_shouldThrowConflict_whenCnpjAlreadyExists() {
        CompanyRequestDTO dto = requestDTO();
        when(companyRepository.existsByCnpj(dto.cnpj())).thenReturn(true);

        Throwable thrown = catchThrowable(() -> companyService.create(dto));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.CONFLICT.value());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowConflict_whenEmailAlreadyExists() {
        CompanyRequestDTO dto = requestDTO();
        when(companyRepository.existsByCnpj(dto.cnpj())).thenReturn(false);
        when(companyRepository.existsByEmail(dto.email())).thenReturn(true);

        Throwable thrown = catchThrowable(() -> companyService.create(dto));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.CONFLICT.value());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenPasswordIsBlank() {
        CompanyRequestDTO dto = new CompanyRequestDTO("Loja X", "12345678000199", "Loja X LTDA",
                "Maria", "contato@lojax.com", "  ", null, null);
        when(companyRepository.existsByCnpj(dto.cnpj())).thenReturn(false);
        when(companyRepository.existsByEmail(dto.email())).thenReturn(false);

        Throwable thrown = catchThrowable(() -> companyService.create(dto));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenEntityRejectsData() {
        CompanyRequestDTO dto = requestDTO();
        when(companyRepository.existsByCnpj(dto.cnpj())).thenReturn(false);
        when(companyRepository.existsByEmail(dto.email())).thenReturn(false);
        when(companyMapper.toEntity(any(), any()))
                .thenThrow(new IllegalArgumentException("Nome é obrigatório"));

        Throwable thrown = catchThrowable(() -> companyService.create(dto));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void findAll_shouldReturnMappedActiveCompanies() {
        when(companyRepository.findAllByActiveTrue()).thenReturn(List.of(company));
        when(companyMapper.toResponseDTO(company)).thenReturn(responseDTO);

        assertThat(companyService.findAll()).containsExactly(responseDTO);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoActiveCompanies() {
        when(companyRepository.findAllByActiveTrue()).thenReturn(List.of());

        assertThat(companyService.findAll()).isEmpty();
    }

    @Test
    void findById_shouldReturnResponseDTO_whenCompanyExists() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMapper.toResponseDTO(company)).thenReturn(responseDTO);

        assertThat(companyService.findById(companyId)).isEqualTo(responseDTO);
    }

    @Test
    void findById_shouldThrowNotFound_whenCompanyDoesNotExist() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> companyService.findById(companyId));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void update_shouldApplyChangesAndReturnResponseDTO_whenCompanyExists() {
        CompanyUpdateDTO dto = updateDTO();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.existsByEmailAndIdNot(dto.email(), companyId)).thenReturn(false);
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toResponseDTO(company)).thenReturn(responseDTO);

        companyService.update(companyId, dto);

        assertThat(company.getName()).isEqualTo("Loja Y");
        assertThat(company.getEmail()).isEqualTo("novo@lojay.com");
        verify(companyRepository).save(company);
    }

    @Test
    void update_shouldThrowNotFound_whenCompanyDoesNotExist() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> companyService.update(companyId, updateDTO()));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowConflict_whenEmailBelongsToAnotherCompany() {
        CompanyUpdateDTO dto = updateDTO();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.existsByEmailAndIdNot(dto.email(), companyId)).thenReturn(true);

        Throwable thrown = catchThrowable(() -> companyService.update(companyId, dto));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.CONFLICT.value());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowBadRequest_whenEntityRejectsData() {
        CompanyUpdateDTO dto = new CompanyUpdateDTO("  ", "Loja Y LTDA", "Joao",
                "novo@lojay.com", null, null);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.existsByEmailAndIdNot(dto.email(), companyId)).thenReturn(false);

        Throwable thrown = catchThrowable(() -> companyService.update(companyId, dto));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.BAD_REQUEST.value());
        verify(companyRepository, never()).save(any());
    }

    @Test
    void deactivate_shouldDeactivateCompany_whenCompanyExists() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        companyService.deactivate(companyId);

        assertThat(company.isActive()).isFalse();
        verify(companyRepository).save(company);
    }

    @Test
    void deactivate_shouldThrowNotFound_whenCompanyDoesNotExist() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> companyService.deactivate(companyId));

        assertThat(statusOf(thrown)).isEqualTo(HttpStatus.NOT_FOUND.value());
        verify(companyRepository, never()).save(any());
    }
}
