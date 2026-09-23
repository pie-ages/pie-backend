package com.ages.pie.domain.enums;

public enum ProductStyle implements TaxonomyItem {
    ROMANTICO("romantico", "Romântico"),
    CLASSICO("classico", "Clássico"),
    CASUAL("casual", "Casual"),
    CRIATIVO("criativo", "Criativo"),
    DRAMATICO("dramatico", "Dramático"),
    REFINADO("refinado", "Refinado");

    private final String id;
    private final String name;

    ProductStyle(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) return false;
        for (ProductStyle s : values()) {
            if (s.id.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
