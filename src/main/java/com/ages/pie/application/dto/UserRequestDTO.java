package com.ages.pie.application.dto;

/**
 * Exemplo de DTO de entrada (request).
 *
 * Representa o formato de dados que chega do front-end (React Native)
 * via JSON. NUNCA deve ser confundido com a Entity de domínio — o DTO
 * pode ter menos campos, campos com nomes diferentes, ou validações de
 * formato (não de negócio) próprias da API.
 *
 * Regra para o time: Controller recebe DTO, nunca Entity. Quem traduz
 * DTO para Entity é o Mapper, não o Controller nem o Service.
 */
public record UserRequestDTO(
    String name,
    String email,
    String password
) {
}