package com.ages.pie.application.dto.wardrobe;

import java.util.List;

public record WardrobeRowDTO(
        String id,
        String title,
        List<WardrobeItemResponseDTO> items,
        boolean hasNext
) {
}