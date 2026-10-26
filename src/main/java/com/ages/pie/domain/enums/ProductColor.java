package com.ages.pie.domain.enums;

public enum ProductColor implements TaxonomyItem {
    BEGE("bege", "Bege", "#D2B48C"),
    AZUL("azul", "Azul", "#0000FF"),
    PRETO("preto", "Preto", "#000000"),
    BRANCO("branco", "Branco", "#FFFFFF"),
    CINZA("cinza", "Cinza", "#808080"),
    VERDE("verde", "Verde", "#008000"),
    VERMELHO("vermelho", "Vermelho", "#FF0000"),
    ROSA("rosa", "Rosa", "#FFC0CB"),
    TERRACOTA("terracota", "Terracota", "#E2725B"),
    OFF_WHITE("off-white", "Off-white", "#FAF0E6"),
    AMARELO("amarelo", "Amarelo", "#FFFF00"),
    LARANJA("laranja", "Laranja", "#FFA500"),
    ROXO("roxo", "Roxo", "#800080"),
    LILAS("lilas", "Lilás", "#C8A2C8"),
    MARROM("marrom", "Marrom", "#8B4513"),
    CARAMELO("caramelo", "Caramelo", "#C68E17"),
    CREME("creme", "Creme", "#FFFDD0"),
    NUDE("nude", "Nude", "#E3BC9A"),
    CORAL("coral", "Coral", "#FF7F50"),
    SALMAO("salmao", "Salmão", "#FA8072"),
    VINHO("vinho", "Vinho", "#800020"),
    BORDO("bordo", "Bordô", "#800000"),
    AZUL_MARINHO("azul-marinho", "Azul-marinho", "#000080"),
    AZUL_CLARO("azul-claro", "Azul-claro", "#87CEFA"),
    AZUL_PETROLEO("azul-petroleo", "Azul-petróleo", "#004B5A"),
    VERDE_OLIVA("verde-oliva", "Verde-oliva", "#808000"),
    VERDE_MUSGO("verde-musgo", "Verde-musgo", "#556B2F"),
    VERDE_MENTA("verde-menta", "Verde-menta", "#98FF98"),
    CINZA_CLARO("cinza-claro", "Cinza-claro", "#D3D3D3"),
    CINZA_ESCURO("cinza-escuro", "Cinza-escuro", "#404040");

    private final String id;
    private final String name;
    private final String referenceHex;

    ProductColor(String id, String name, String referenceHex) {
        this.id = id;
        this.name = name;
        this.referenceHex = referenceHex;
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    public String getReferenceHex() { return referenceHex; }

    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) return false;
        for (ProductColor c : values()) {
            if (c.id.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}