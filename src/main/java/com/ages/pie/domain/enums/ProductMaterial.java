package com.ages.pie.domain.enums;

public enum ProductMaterial implements TaxonomyItem {
    ALGODAO("algodao", "Algodão"),
    LINHO("linho", "Linho"),
    POLIESTER("poliester", "Poliéster"),
    VISCOSE("viscose", "Viscose"),
    SEDA("seda", "Seda"),
    DENIM("denim", "Denim"),
    LA("la", "Lã"),
    TRICOT("tricot", "Tricot"),
    COURO("couro", "Couro");

    private final String id;
    private final String name;

    ProductMaterial(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) return false;
        for (ProductMaterial m : values()) {
            if (m.id.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
