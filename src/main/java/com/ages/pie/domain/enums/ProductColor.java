package com.ages.pie.domain.enums;

public enum ProductColor implements TaxonomyItem {
    BEGE("bege", "Bege"),
    AZUL("azul", "Azul"),
    PRETO("preto", "Preto"),
    BRANCO("branco", "Branco"),
    CINZA("cinza", "Cinza"),
    VERDE("verde", "Verde"),
    VERMELHO("vermelho", "Vermelho"),
    ROSA("rosa", "Rosa"),
    TERRACOTA("terracota", "Terracota"),
    OFF_WHITE("off-white", "Off-white");

    private final String id;
    private final String name;

    ProductColor(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) return false;
        for (ProductColor c : values()) {
            if (c.id.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
