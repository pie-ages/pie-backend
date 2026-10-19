package com.ages.pie.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    private final Product product = new Product(
            new Company(UUID.randomUUID(), "Loja", "12345678000100", "Loja LTDA", "Ana", "loja@teste.com", "hash", null, null),
            "Produto");

    @Test
    void applyAnalysis_shouldFillEmptyFields() {
        product.applyAnalysis("vestido", "romantico", "verde");

        assertThat(product.getCategory()).isEqualTo("vestido");
        assertThat(product.getStyles()).containsExactly("romantico");
        assertThat(product.getColor()).isEqualTo("verde");
    }

    @Test
    void applyAnalysis_shouldKeepFilledFields() {
        product.setCategory("blazer");
        product.setStyles(List.of("classico"));
        product.setColor("preto");

        product.applyAnalysis("vestido", "romantico", "verde");

        assertThat(product.getCategory()).isEqualTo("blazer");
        assertThat(product.getStyles()).containsExactly("classico");
        assertThat(product.getColor()).isEqualTo("preto");
    }

    @Test
    void applyAnalysis_shouldIgnoreNullValues() {
        product.applyAnalysis(null, null, null);

        assertThat(product.getCategory()).isNull();
        assertThat(product.getStyles()).isEmpty();
        assertThat(product.getColor()).isNull();
    }
}
