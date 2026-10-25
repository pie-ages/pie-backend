package com.ages.pie.application.dto.look;

import java.util.UUID;

/**
 * Referência a uma peça na posição em que o usuário a adicionou. Exatamente um dos ids deve ser informado.
 */
public record LookItemRefDTO(
    UUID wardrobeItemId,
    UUID productId
) {
}
