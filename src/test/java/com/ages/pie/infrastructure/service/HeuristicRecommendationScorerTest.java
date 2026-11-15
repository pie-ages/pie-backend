package com.ages.pie.infrastructure.service;

import com.ages.pie.domain.entity.Product;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeuristicRecommendationScorerTest {

    private final HeuristicRecommendationScorer scorer = new HeuristicRecommendationScorer();

    private Product product(List<String> styles, String color) {
        Product product = mock(Product.class);
        when(product.getStyles()).thenReturn(styles);
        when(product.getColor()).thenReturn(color);
        return product;
    }

    @Test
    void semPreferenciasPontuaZero() {
        assertEquals(0.0, scorer.score(Set.of(), Set.of(), product(List.of("casual"), "azul")), 1e-9);
    }

    @Test
    void pontuacaoProporcionalAosEstilos() {
        Product product = product(List.of("casual", "refinado"), null);
        assertEquals(50.0, scorer.score(Set.of("casual", "refinado"), Set.of(), product), 1e-9);
        assertEquals(25.0, scorer.score(Set.of("casual"), Set.of(), product), 1e-9);
        assertEquals(0.0, scorer.score(Set.of("dramatico"), Set.of(), product), 1e-9);
    }

    @Test
    void corFavoritaPontua50NormalizandoCaixaEEspacos() {
        Product product = product(List.of(), "  Azul ");
        assertEquals(50.0, scorer.score(Set.of(), Set.of("azul"), product), 1e-9);
        assertEquals(0.0, scorer.score(Set.of(), Set.of("preto"), product), 1e-9);
    }

    @Test
    void estilosDoProdutoSaoNormalizadosAntesDeComparar() {
        Product product = product(List.of(" Casual "), null);
        assertEquals(50.0, scorer.score(Set.of("casual"), Set.of(), product), 1e-9);
    }

    @Test
    void pontuacaoMaximaComEstiloECor() {
        Product product = product(List.of("casual"), "azul");
        assertEquals(100.0, scorer.score(Set.of("casual"), Set.of("azul"), product), 1e-9);
    }

    @Test
    void produtoSemEstilosNaoPontuaEstilo() {
        Product product = product(List.of(), "azul");
        assertEquals(50.0, scorer.score(Set.of("casual"), Set.of("azul"), product), 1e-9);
    }
}
