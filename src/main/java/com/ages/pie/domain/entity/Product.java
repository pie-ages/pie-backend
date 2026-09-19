package com.ages.pie.domain.entity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.ages.pie.domain.enums.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "product")
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String name;

    private String description;

    private String category;

    private String color;

    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "purchase_url")
    private String purchaseUrl;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatus status = ProductStatus.RASCUNHO;

    protected Product() {
    }

    public Product(Company company, String name) {
        this.company = Objects.requireNonNull(company, "Empresa é obrigatória");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getColor() {
        return color;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public boolean isActive() {
        return active;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
