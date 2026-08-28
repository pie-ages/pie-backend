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

    @Column(name = "is_ai_generated")
    private boolean aiGenerated = false;

    private String occasion;

    @Column(name = "photo_url")
    private String photoUrl;

    @OneToMany(mappedBy = "look", fetch = FetchType.LAZY)
    private List<LookWardrobeItem> wardrobeItems = new ArrayList<>();

    @OneToMany(mappedBy = "look", fetch = FetchType.LAZY)
    private List<LookProduct> products = new ArrayList<>();

    protected Look() {
    }

    public Look(User customer, String title) {
        this.customer = Objects.requireNonNull(customer, "Cliente é obrigatório");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        this.title = title;
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

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public String getOccasion() {
        return occasion;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public List<LookWardrobeItem> getWardrobeItems() {
        return wardrobeItems;
    }

    public List<LookProduct> getProducts() {
        return products;
    }
}
