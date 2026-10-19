package com.ages.pie.domain.service;

import com.ages.pie.domain.enums.ProductColor;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductColorMapperTest {

    @Test
    void shouldMapEveryReferenceToItsOwnColor() {
        for (ProductColor color : ProductColor.values()) {
            assertThat(ProductColorMapper.fromHex(color.getReferenceHex()))
                    .isEqualTo(color);
        }
    }

    @Test
    void shouldAcceptLowercaseHexadecimal() {
        for (ProductColor color : ProductColor.values()) {
            String lowercase = color.getReferenceHex().toLowerCase(Locale.ROOT);

            assertThat(ProductColorMapper.fromHex(lowercase))
                    .isEqualTo(color);
        }
    }

    @Test
    void shouldMapDifferentBlueShadesToNavyBlue() {
        assertThat(ProductColorMapper.fromHex("#1241B6"))
                .isEqualTo(ProductColor.AZUL_MARINHO);

        assertThat(ProductColorMapper.fromHex("#173AA0"))
                .isEqualTo(ProductColor.AZUL_MARINHO);
    }

    @Test
    void shouldMapNearbyShadesToNewColors() {
        assertThat(ProductColorMapper.fromHex("#FEFE01"))
                .isEqualTo(ProductColor.AMARELO);

        assertThat(ProductColorMapper.fromHex("#FEA401"))
                .isEqualTo(ProductColor.LARANJA);

        assertThat(ProductColorMapper.fromHex("#C7A1C7"))
                .isEqualTo(ProductColor.LILAS);

        assertThat(ProductColorMapper.fromHex("#97FE97"))
                .isEqualTo(ProductColor.VERDE_MENTA);

        assertThat(ProductColorMapper.fromHex("#414141"))
                .isEqualTo(ProductColor.CINZA_ESCURO);
    }

    @Test
    void shouldUseEnumOrderWhenDistancesAreEqual() {
        assertThat(ProductColorMapper.fromHex("#004000"))
                .isEqualTo(ProductColor.PRETO);
    }

    @Test
    void shouldRejectInvalidHexadecimal() {
        for (String invalid : new String[]{
                "", "#FFF", "FF0000", "#GG0000", "#FF000080", " #FF0000"
        }) {
            assertThatThrownBy(() -> ProductColorMapper.fromHex(invalid))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> ProductColorMapper.fromHex(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}