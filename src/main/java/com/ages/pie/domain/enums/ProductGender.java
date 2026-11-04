package com.ages.pie.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProductGender {
    FEMININO,
    MASCULINO,
    UNISSEX;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ProductGender fromValue(String value) {
        return ProductGender.valueOf(value);
    }
}