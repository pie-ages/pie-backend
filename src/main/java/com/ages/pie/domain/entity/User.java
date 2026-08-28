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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "customer")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String name;

    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "photo_url")
    private String photoUrl;

    @OneToOne(mappedBy = "customer", fetch = FetchType.LAZY)
    private BodyProfile bodyProfile;

    @OneToOne(mappedBy = "customer", fetch = FetchType.LAZY)
    private Wishlist wishlist;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<WardrobeItem> wardrobeItems = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Look> looks = new ArrayList<>();

    protected User() {
    }

    public User(String name, String email, String passwordHash) {
        this.name = validateName(name);
        this.email = validateEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "Senha não pode ser nula");
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

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public BodyProfile getBodyProfile() {
        return bodyProfile;
    }

    public Wishlist getWishlist() {
        return wishlist;
    }

    public List<WardrobeItem> getWardrobeItems() {
        return wardrobeItems;
    }

    public List<Look> getLooks() {
        return looks;
    }
}
