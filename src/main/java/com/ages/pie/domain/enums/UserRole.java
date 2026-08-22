package com.ages.pie.domain.enums;

/**
 * Exemplo de Enum de domínio.
 *
 * Representa um conjunto fechado de estados/papéis de negócio. Usar enum
 * em vez de String solta ("cliente", "admin" digitado à mão em vários
 * lugares) evita erro de digitação e deixa o compilador Java pegar erro
 * que só apareceria em tempo de execução.
 *
 * Regra para o time: se você notar um if/else ou switch comparando String
 * pra representar um estado de negócio, provavelmente devia ser um enum.
 */
public enum UserRole {
    CLIENTE,
    EMPRESA_ADMIN,
    ADMIN
}
