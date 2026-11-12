package com.ages.pie.domain.entity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.ages.pie.domain.enums.CompanyRole;
import com.ages.pie.domain.enums.CompanyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Column(name = "supabase_user_id", unique = true)
    private UUID supabaseUserId;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private CompanyRole role = CompanyRole.COMPANY;

    @Enumerated(EnumType.STRING)
    private CompanyStatus status = CompanyStatus.PENDING;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "photo_storage_key")
    private String photoStorageKey;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    protected Company() {
    }

    public Company(UUID id, String name, String cnpj, String socialReason, String responsiblePerson,
            String email, UUID supabaseUserId, String website, String photoUrl) {
        this.id = id;
        this.name = requireText(name, "Nome é obrigatório");
        this.cnpj = requireText(cnpj, "CNPJ é obrigatório");
        this.socialReason = requireText(socialReason, "Razão social é obrigatória");
        this.responsiblePerson = requireText(responsiblePerson, "Responsável é obrigatório");
        this.email = requireEmail(email);
        this.supabaseUserId = Objects.requireNonNull(supabaseUserId, "Identificador do Supabase é obrigatório");
        this.website = website;
        this.photoUrl = optionalUrl(photoUrl, "URL da logo inválida");
    }

    public void update(String name, String socialReason, String responsiblePerson,
            String email, String website) {
        this.name = requireText(name, "Nome é obrigatório");
        this.socialReason = requireText(socialReason, "Razão social é obrigatória");
        this.responsiblePerson = requireText(responsiblePerson, "Responsável é obrigatório");
        this.email = requireEmail(email);
        this.website = website;
    }

    public void updateLogo(String photoUrl, String photoStorageKey) {
        this.photoUrl = photoUrl;
        this.photoStorageKey = photoStorageKey;
    }

    public void clearLogo() {
        this.photoUrl = null;
        this.photoStorageKey = null;
    }

    public void linkSupabaseUser(UUID supabaseUserId) {
        this.supabaseUserId = Objects.requireNonNull(supabaseUserId);
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateStatus(CompanyStatus newStatus) {
        this.status = newStatus;
        this.active = newStatus == CompanyStatus.ACTIVE;
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

    private String optionalUrl(String url, String message) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (!isHttpUrl(url)) {
            throw new IllegalArgumentException(message);
        }
        return url;
    }

    private boolean isHttpUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost() != null
                    && ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()));
        } catch (URISyntaxException e) {
            return false;
        }
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

    public UUID getSupabaseUserId() {
        return supabaseUserId;
    }

    public boolean isActive() {
        return active;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getPhotoStorageKey() {
        return photoStorageKey;
    }

    public CompanyRole getRole() {
        return role;
    }

    public CompanyStatus getStatus() {
        return status;
    }

    public List<Product> getProducts() {
        return products;
    }
}
