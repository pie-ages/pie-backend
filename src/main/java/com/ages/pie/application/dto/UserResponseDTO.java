package com.ages.pie.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Exemplo de DTO de saída (response).
 *
 * Repare que NÃO tem o campo passwordHash — o DTO de resposta é também
 * o lugar certo para decidir o que a API expõe para fora. Nunca serialize
 * a Entity direto no Controller, ou um dia alguém esquece um campo
 * sensível e ele vaza na resposta HTTP sem querer.
 */
public record UserResponseDTO(
    UUID id,
    String name,
    String email,
    String photoUrl,
    OffsetDateTime createdAt
) {
}