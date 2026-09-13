package com.ages.pie.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import com.ages.pie.domain.enums.UserRole;

/**
 * Exemplo de Aggregate Root.
 *
 * Representa um conceito central do domínio, com identidade própria (id) e
 * ciclo de vida independente. É responsável por proteger seus próprios
 * invariantes: um User nunca deve existir em estado inválido (sem nome,
 * sem email válido, etc.), então essas validações ficam no construtor,
 * não em quem cria o objeto.
 *
 * Regra para o time: não crie setters soltos que permitam colocar a
 * entidade em estado inconsistente. Se um atributo pode mudar, crie um
 * método de negócio com nome (ex: promoverPara), não um setPapel genérico.
 */
public class User {

    private Long id;
    private String name;
    private String email;
    private String passwordHash;
    private UserRole role;
    private LocalDateTime createdAt;


    /** Construtor protegido, exigido pelo JPA. Não usar diretamente. */
    protected User() {
    }

    public User(String name, String email, String passwordHash, UserRole role) {
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "Senha não pode ser nula");
        this.role = Objects.requireNonNull(role, "Papel do usuário é obrigatório");
        this.createdAt = LocalDateTime.now();
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        return name;
    }

    private String validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        return email;
    }

    /**
     * Exemplo de método de negócio nomeado, em vez de um setter genérico.
     * Deixa explícito, no código e no log de commits, que uma promoção
     * de papel é uma ação de domínio, não uma edição de campo qualquer.
     */
    public void promoverPara(UserRole novoRole) {
        this.role = Objects.requireNonNull(novoRole, "Novo papel não pode ser nulo");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}