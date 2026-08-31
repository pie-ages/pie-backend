package com.ages.pie.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CompanyTest {

    private Company empresaValida() {
        return new Company("Loja X", "12345678000199", "Loja X LTDA", "Maria",
            "contato@lojax.com", "hash", "https://lojax.com", null);
    }

    @Test
    void criaComDadosValidos() {
        Company company = empresaValida();

        assertThat(company.getName()).isEqualTo("Loja X");
        assertThat(company.getCnpj()).isEqualTo("12345678000199");
        assertThat(company.getSocialReason()).isEqualTo("Loja X LTDA");
        assertThat(company.getResponsiblePerson()).isEqualTo("Maria");
        assertThat(company.getEmail()).isEqualTo("contato@lojax.com");
        assertThat(company.getWebsite()).isEqualTo("https://lojax.com");
        assertThat(company.getPhotoUrl()).isNull();
        assertThat(company.isActive()).isTrue();
    }

    @Test
    void rejeitaNomeEmBranco() {
        assertThatThrownBy(() -> new Company("  ", "123", "Razao", "Maria",
            "a@b.com", "hash", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Nome é obrigatório");
    }

    @Test
    void rejeitaCnpjNulo() {
        assertThatThrownBy(() -> new Company("Loja", null, "Razao", "Maria",
            "a@b.com", "hash", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CNPJ é obrigatório");
    }

    @Test
    void rejeitaRazaoSocialEmBranco() {
        assertThatThrownBy(() -> new Company("Loja", "123", " ", "Maria",
            "a@b.com", "hash", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Razão social é obrigatória");
    }

    @Test
    void rejeitaResponsavelNulo() {
        assertThatThrownBy(() -> new Company("Loja", "123", "Razao", null,
            "a@b.com", "hash", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Responsável é obrigatório");
    }

    @Test
    void rejeitaEmailSemArroba() {
        assertThatThrownBy(() -> new Company("Loja", "123", "Razao", "Maria",
            "invalido", "hash", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email inválido");
    }

    @Test
    void rejeitaSenhaNula() {
        assertThatThrownBy(() -> new Company("Loja", "123", "Razao", "Maria",
            "a@b.com", null, null, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void atualizaDados() {
        Company company = empresaValida();

        company.atualizarDados("Loja Y", "Loja Y LTDA", "Joao", "novo@lojay.com",
            "https://lojay.com", "https://img/y.png");

        assertThat(company.getName()).isEqualTo("Loja Y");
        assertThat(company.getSocialReason()).isEqualTo("Loja Y LTDA");
        assertThat(company.getResponsiblePerson()).isEqualTo("Joao");
        assertThat(company.getEmail()).isEqualTo("novo@lojay.com");
        assertThat(company.getWebsite()).isEqualTo("https://lojay.com");
        assertThat(company.getPhotoUrl()).isEqualTo("https://img/y.png");
    }

    @Test
    void atualizarDadosRejeitaEmailInvalido() {
        Company company = empresaValida();

        assertThatThrownBy(() -> company.atualizarDados("Loja Y", "Razao", "Joao",
            "semarroba", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email inválido");
    }

    @Test
    void desativa() {
        Company company = empresaValida();

        company.desativar();

        assertThat(company.isActive()).isFalse();
    }
}
