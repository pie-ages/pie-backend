package com.ages.pie.domain.service;

import com.ages.pie.domain.enums.ProductColor;

//Recebe um HEXA e devolver um ProductColor
public final class ProductColorMapper {

    private ProductColorMapper() {
    }

    public static ProductColor fromHex(String hex) {
        int rgb = parseHex(hex);
        ProductColor nearest = null;
        int shortestDistance = Integer.MAX_VALUE;

        for (ProductColor color : ProductColor.values()) {
            int reference = parseHex(color.getReferenceHex());
            int distance = squaredDistance(rgb, reference);

            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearest = color;
            }
        }

        return nearest;
    }

    private static int parseHex(String hex) {
        if (hex == null || !hex.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("Cor inválida. Use formato hexadecimal: #RRGGBB");
        }

        return Integer.parseInt(hex.substring(1), 16);
    }

    private static int squaredDistance(int color, int reference) {
        int redDifference = ((color >> 16) & 255) - ((reference >> 16) & 255);
        int greenDifference = ((color >> 8) & 255) - ((reference >> 8) & 255);
        int blueDifference = (color & 255) - (reference & 255);

        return ((redDifference * redDifference) + (greenDifference * greenDifference) + (blueDifference * blueDifference));
    }
}