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
@Table(name = "look_product", uniqueConstraints = @UniqueConstraint(
        columnNames = { "look_id", "product_id" }))
public class LookProduct {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "look_id", nullable = false)
    private Look look;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected LookProduct() {
    }

    public LookProduct(Look look, Product product) {
        this.look = Objects.requireNonNull(look, "Look é obrigatório");
        this.product = Objects.requireNonNull(product, "Produto é obrigatório");
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Look getLook() {
        return look;
    }

    public Product getProduct() {
        return product;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
