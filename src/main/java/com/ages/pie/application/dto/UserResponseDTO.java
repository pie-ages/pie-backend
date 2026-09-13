package com.ages.pie.application.dto;

import com.ages.pie.domain.enums.UserRole;

import java.time.LocalDateTime;

/**
 * Exemplo de DTO de saída (response).
 *
 * Repare que NÃO tem o campo passwordHash — o DTO de resposta é também
 * o lugar certo para decidir o que a API expõe para fora. Nunca serialize
 * a Entity direto no Controller, ou um dia alguém esquece um campo
 * sensível e ele vaza na resposta HTTP sem querer.
 */
public record UserResponseDTO(
    Long id,
    String name,
    String email,
    UserRole role,
    LocalDateTime createdAt
) {
}