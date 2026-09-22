package com.ages.pie.domain.enums;

public enum ProductSize implements TaxonomyItem {
    PP("pp", "PP"),
    P("p", "P"),
    M("m", "M"),
    G("g", "G"),
    GG("gg", "GG"),
    XGG("xgg", "XGG"),
    U("u", "U"),
    N33("33", "33"),
    N34("34", "34"),
    N35("35", "35"),
    N36("36", "36"),
    N37("37", "37"),
    N38("38", "38"),
    N39("39", "39"),
    N40("40", "40"),
    N41("41", "41"),
    N42("42", "42"),
    N44("44", "44");

    private final String id;
    private final String name;

    ProductSize(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) return false;
        for (ProductSize s : values()) {
            if (s.id.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
