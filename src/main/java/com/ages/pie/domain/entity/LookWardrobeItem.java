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
@Table(name = "look_wardrobe_item", uniqueConstraints = @UniqueConstraint(
        columnNames = { "look_id", "wardrobe_item_id" }))
public class LookWardrobeItem {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "look_id", nullable = false)
    private Look look;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wardrobe_item_id", nullable = false)
    private WardrobeItem wardrobeItem;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    protected LookWardrobeItem() {
    }

    public LookWardrobeItem(Look look, WardrobeItem wardrobeItem) {
        this.look = Objects.requireNonNull(look, "Look é obrigatório");
        this.wardrobeItem = Objects.requireNonNull(wardrobeItem, "Peça do guarda-roupa é obrigatória");
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

    public WardrobeItem getWardrobeItem() {
        return wardrobeItem;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
