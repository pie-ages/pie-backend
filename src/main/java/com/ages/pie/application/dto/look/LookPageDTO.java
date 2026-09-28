package com.ages.pie.application.dto.look;

import java.util.List;

public record LookPageDTO(
    List<LookResponseDTO> items,
    long total,
    int page,
    int size,
    boolean hasNext
) {
}