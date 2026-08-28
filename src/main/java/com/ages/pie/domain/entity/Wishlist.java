package com.ages.pie.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "wishlist")
public class Wishlist extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private User customer;

    private String name;

    @OneToMany(mappedBy = "wishlist", fetch = FetchType.LAZY)
    private List<WishlistItem> items = new ArrayList<>();

    protected Wishlist() {
    }

    public Wishlist(User customer, String name) {
        this.customer = Objects.requireNonNull(customer, "Cliente é obrigatório");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public String getName() {
        return name;
    }

    public List<WishlistItem> getItems() {
        return items;
    }
}
