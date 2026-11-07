package com.ages.pie.application.mapper;

import com.ages.pie.application.dto.company.CompanyRequestDTO;
import com.ages.pie.application.dto.company.CompanyResponseDTO;
import com.ages.pie.domain.entity.Company;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompanyMapperTest {

    private static final String LOGO_URL =
            "https://abcd.supabase.co/storage/v1/object/public/company-logos/companies/1/logo.png";

    private final CompanyMapper companyMapper = new CompanyMapper();

    private CompanyRequestDTO requestDTO(String photoUrl) {
        return new CompanyRequestDTO("Loja X", "12345678000199", "Loja X LTDA", "Maria",
                "contato@lojax.com", "senha123", null, photoUrl);
    }

    @Test
    void toEntity_shouldKeepPhotoUrl_whenUrlIsValid() {
        Company company = companyMapper.toEntity(requestDTO(LOGO_URL), UUID.randomUUID());

        assertThat(company.getPhotoUrl()).isEqualTo(LOGO_URL);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void toEntity_shouldSetPhotoUrlToNull_whenPhotoUrlIsMissing(String photoUrl) {
        Company company = companyMapper.toEntity(requestDTO(photoUrl), UUID.randomUUID());

        assertThat(company.getPhotoUrl()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "logo.png", "/storage/v1/object/public/logo.png", "ftp://servidor.com/logo.png",
            "javascript:alert(1)", "https://", "https://site.com/minha logo.png" })
    void toEntity_shouldThrowIllegalArgumentException_whenPhotoUrlIsInvalid(String photoUrl) {
        CompanyRequestDTO dto = requestDTO(photoUrl);
        UUID supabaseUserId = UUID.randomUUID();

        assertThatThrownBy(() -> companyMapper.toEntity(dto, supabaseUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("URL da logo inválida");
    }

    @Test
    void toResponseDTO_shouldExposePhotoUrl() {
        Company company = companyMapper.toEntity(requestDTO(LOGO_URL), UUID.randomUUID());

        CompanyResponseDTO dto = companyMapper.toResponseDTO(company);

        assertThat(dto.photoUrl()).isEqualTo(LOGO_URL);
    }

    @Test
    void toResponseDTO_shouldReturnNullPhotoUrl_whenCompanyHasNoLogo() {
        Company company = companyMapper.toEntity(requestDTO(null), UUID.randomUUID());

        CompanyResponseDTO dto = companyMapper.toResponseDTO(company);

        assertThat(dto.photoUrl()).isNull();
    }
}
