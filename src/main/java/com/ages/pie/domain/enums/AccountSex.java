package com.ages.pie.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountSex {
    MASCULINO,
    FEMININO,
    PREFIRO_NAO_INFORMAR;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AccountSex fromValue(String value) {
        return AccountSex.valueOf(value);
    }
}