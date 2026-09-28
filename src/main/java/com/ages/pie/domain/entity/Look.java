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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "look")
public class Look extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    private String title;

    private String description;

    @Column(name = "is_ai_generated")
    private boolean aiGenerated = false;

    private String occasion;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "photo_storage_key")
    private String photoStorageKey;

    @OneToMany(mappedBy = "look", fetch = FetchType.LAZY)
    @OrderBy("createdAt")
    private List<LookWardrobeItem> wardrobeItems = new ArrayList<>();

    @OneToMany(mappedBy = "look", fetch = FetchType.LAZY)
    @OrderBy("createdAt")
    private List<LookProduct> products = new ArrayList<>();

    protected Look() {
    }

    public Look(User customer, String title, String description, String occasion) {
        this.customer = Objects.requireNonNull(customer, "Cliente é obrigatório");
        this.title = requireTitle(title);
        this.description = description;
        this.occasion = occasion;
    }

    public void update(String title, String description, String occasion) {
        this.title = requireTitle(title);
        this.description = description;
        this.occasion = occasion;
    }

    public void updatePhoto(String photoUrl, String photoStorageKey) {
        this.photoUrl = photoUrl;
        this.photoStorageKey = photoStorageKey;
    }

    public void clearPhoto() {
        this.photoUrl = null;
        this.photoStorageKey = null;
    }

    private String requireTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        return title;
    }

    public UUID getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public String getOccasion() {
        return occasion;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getPhotoStorageKey() {
        return photoStorageKey;
    }

    public List<LookWardrobeItem> getWardrobeItems() {
        return wardrobeItems;
    }

    public List<LookProduct> getProducts() {
        return products;
    }
}
