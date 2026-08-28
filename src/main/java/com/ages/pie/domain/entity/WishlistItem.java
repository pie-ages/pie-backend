package com.ages.pie.domain.entity;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "wishlist_item", uniqueConstraints = @UniqueConstraint(
        name = "uq_wishlist_product", columnNames = { "wishlist_id", "product_id" }))
public class WishlistItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    protected WishlistItem() {
    }

    public WishlistItem(Wishlist wishlist, Product product) {
        this.wishlist = Objects.requireNonNull(wishlist, "Wishlist é obrigatória");
        this.product = Objects.requireNonNull(product, "Produto é obrigatório");
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Wishlist getWishlist() {
        return wishlist;
    }

    public Product getProduct() {
        return product;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
