package com.ages.pie.domain.enums;

import java.util.List;

public enum ColorimetrySeason {

    SPRING("Primavera",
        List.of("#FFCBA4", "#F4A460", "#E8C99A", "#FFD700", "#FFA07A"),
        List.of("#000080", "#4B0082", "#008B8B", "#2F4F4F"),
        "Tons quentes e claros valorizam sua pele.",
        "Evite tons frios e escuros próximos ao rosto."),

    SUMMER("Verão",
        List.of("#B0C4DE", "#E6E6FA", "#DDA0DD", "#C0C0C0", "#87CEEB"),
        List.of("#FF4500", "#8B0000", "#FF8C00", "#DAA520"),
        "Tons frios e suaves harmonizam com sua pele.",
        "Tons quentes e saturados criam contraste excessivo."),

    AUTUMN("Outono",
        List.of("#D2691E", "#CD853F", "#A0522D", "#8B4513", "#DEB887"),
        List.of("#FF69B4", "#00CED1", "#7B68EE", "#E0E0E0"),
        "Tons terrosos e quentes realçam sua beleza.",
        "Tons frios e pastel apagam seu tom de pele."),

    WINTER("Inverno",
        List.of("#DC143C", "#000080", "#800080", "#008000", "#1C1C1C"),
        List.of("#FFDEAD", "#F0E68C", "#F5DEB3", "#FFFACD"),
        "Tons vivos e contrastantes destacam sua pele.",
        "Tons quentes e acobreados desharmonizam com você.");

    private final String label;
    private final List<String> highlightColors;
    private final List<String> avoidColors;
    private final String highlightCaption;
    private final String avoidCaption;

    ColorimetrySeason(String label, List<String> highlightColors, List<String> avoidColors,
                      String highlightCaption, String avoidCaption) {
        this.label = label;
        this.highlightColors = highlightColors;
        this.avoidColors = avoidColors;
        this.highlightCaption = highlightCaption;
        this.avoidCaption = avoidCaption;
    }

    public String getLabel() { return label; }
    public List<String> getHighlightColors() { return highlightColors; }
    public List<String> getAvoidColors() { return avoidColors; }
    public String getHighlightCaption() { return highlightCaption; }
    public String getAvoidCaption() { return avoidCaption; }
}
