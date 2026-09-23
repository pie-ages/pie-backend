package com.ages.pie.domain.enums;

public enum ProductCategory implements TaxonomyItem {
    VESTIDO("vestido", "Vestido"),
    BLAZER("blazer", "Blazer"),
    CAMISA("camisa", "Camisa"),
    CAMISETA("camiseta", "Camiseta"),
    BLUSA("blusa", "Blusa"),
    CALCA("calca", "Calça"),
    SAIA("saia", "Saia"),
    SHORT("short", "Short"),
    BERMUDA("bermuda", "Bermuda"),
    CASACO("casaco", "Casaco"),
    JAQUETA("jaqueta", "Jaqueta"),
    MOLETOM("moletom", "Moletom"),
    SAPATO("sapato", "Sapato");

    private final String id;
    private final String name;

    ProductCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) return false;
        for (ProductCategory c : values()) {
            if (c.id.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
