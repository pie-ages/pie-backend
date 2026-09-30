package com.ages.pie.domain.entity;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "wardrobe_item")
public class WardrobeItem extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String name;

    private String category;

    private String style;

    private String color;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "storage_key")
    private String storageKey;

    protected WardrobeItem() {
    }

    public WardrobeItem(User customer, Product product, String name, String category, String style, String color) {
        this.customer = Objects.requireNonNull(customer, "Cliente é obrigatório");
        this.name = Objects.requireNonNull(name, "Nome é obrigatório");
        this.product = product;
        this.category = category;
        this.style = style;
        this.color = color;
    }

    public UUID getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public Product getProduct() {
        return product;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getStyle() {
        return style;
    }

    public String getColor() {
        return color;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void update(Product product, String name, String category, String style, String color) {
        this.product = product;
        this.name = Objects.requireNonNull(name, "Nome é obrigatório");
        this.category = category;
        this.style = style;
        this.color = color;
    }

    public void setImageReference(String photoUrl, String storageKey) {
        this.photoUrl = photoUrl;
        this.storageKey = storageKey;
    }
}
