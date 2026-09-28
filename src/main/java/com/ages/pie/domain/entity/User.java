package com.ages.pie.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.ages.pie.domain.enums.Style;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

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

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "style_result", columnDefinition = "varchar[]")
    private List<String> styleResult = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "style_preference", columnDefinition = "varchar[]", nullable = false)
    private List<String> stylePreference = new ArrayList<>();

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

    public void update(String name, String photoUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.photoUrl = photoUrl;
    }

    public void updateStyleResult(List<String> styleResult) {
        this.styleResult = styleResult == null ? new ArrayList<>() : new ArrayList<>(styleResult);
    }

    public void updateStylePreference(List<Style> styles) {
        this.stylePreference = new ArrayList<>(Objects.requireNonNull(styles, "Estilos são obrigatórios").stream()
            .map(style -> Objects.requireNonNull(style, "Estilo não pode ser nulo").name())
            .toList());
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

    public List<String> getStyleResult() {
        return styleResult;
    }

    public List<Style> getStylePreference() {
        return stylePreference == null ? List.of() : stylePreference.stream().map(Style::valueOf).toList();
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
