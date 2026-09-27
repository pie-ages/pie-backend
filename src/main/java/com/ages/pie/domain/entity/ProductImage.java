package com.ages.pie.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "product_image")
public class ProductImage extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String url;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    protected ProductImage() {}

    public ProductImage(Product product, String url, String storageKey, boolean isPrimary, int displayOrder) {
        this.product = product;
        this.url = url;
        this.storageKey = storageKey;
        this.isPrimary = isPrimary;
        this.displayOrder = displayOrder;
    }

    public UUID getId() { return id; }

    public Product getProduct() { return product; }

    public String getUrl() { return url; }

    public String getStorageKey() { return storageKey; }

    public boolean isPrimary() { return isPrimary; }

    public void setPrimary(boolean isPrimary) { this.isPrimary = isPrimary; }

    public int getDisplayOrder() { return displayOrder; }

    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
