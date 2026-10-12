package com.ages.pie.application.dto.company;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyRequestDTOTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private CompanyRequestDTO withEmail(String email) {
        return new CompanyRequestDTO("Loja X", "12345678000199", "Loja X LTDA", "Maria",
                email, "senha123", null, null);
    }

    @Test
    void shouldRejectNullEmail() {
        assertThat(VALIDATOR.validate(withEmail(null))).isNotEmpty();
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThat(VALIDATOR.validate(withEmail("   "))).isNotEmpty();
    }

    @Test
    void shouldRejectMalformedEmail() {
        assertThat(VALIDATOR.validate(withEmail("sem-arroba"))).isNotEmpty();
    }

    @Test
    void shouldAcceptValidEmail() {
        assertThat(VALIDATOR.validate(withEmail("contato@lojax.com"))).isEmpty();
    }
}
