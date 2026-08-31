package com.ages.pie.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "company")
public class Company extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String name;

    @Column(unique = true)
    private String cnpj;

    private String website;

    @Column(name = "social_reason")
    private String socialReason;

    @Column(name = "responsible_person")
    private String responsiblePerson;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    private boolean active = true;

    @Column(name = "photo_url")
    private String photoUrl;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    protected Company() {
    }

    public Company(String name, String cnpj, String socialReason, String responsiblePerson,
            String email, String passwordHash, String website, String photoUrl) {
        this.name = requireText(name, "Nome é obrigatório");
        this.cnpj = requireText(cnpj, "CNPJ é obrigatório");
        this.socialReason = requireText(socialReason, "Razão social é obrigatória");
        this.responsiblePerson = requireText(responsiblePerson, "Responsável é obrigatório");
        this.email = requireEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "Senha não pode ser nula");
        this.website = website;
        this.photoUrl = photoUrl;
    }

    public void atualizarDados(String name, String socialReason, String responsiblePerson,
            String email, String website, String photoUrl) {
        this.name = requireText(name, "Nome é obrigatório");
        this.socialReason = requireText(socialReason, "Razão social é obrigatória");
        this.responsiblePerson = requireText(responsiblePerson, "Responsável é obrigatório");
        this.email = requireEmail(email);
        this.website = website;
        this.photoUrl = photoUrl;
    }

    public void desativar() {
        this.active = false;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String requireEmail(String email) {
        requireText(email, "Email é obrigatório");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }
        return email;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getWebsite() {
        return website;
    }

    public String getSocialReason() {
        return socialReason;
    }

    public String getResponsiblePerson() {
        return responsiblePerson;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return active;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public List<Product> getProducts() {
        return products;
    }
}
