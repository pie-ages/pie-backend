package com.ages.pie.domain.entity;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "body_profile")
public class BodyProfile extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private User customer;

    @Column(name = "body_shape")
    private String bodyShape;

    @Column(name = "kibbe_type")
    private String kibbeType;

    @Column(name = "color_palette")
    private String colorPalette;

    @Column(name = "zyla_palette")
    private String zylaPalette;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "style_preference", columnDefinition = "varchar[]")
    private String[] stylePreference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> measurements;

    @Column(name = "ai_analysis_s3_key")
    private String aiAnalysisS3Key;

    protected BodyProfile() {
    }

    public BodyProfile(User customer) {
        this.customer = Objects.requireNonNull(customer, "Cliente é obrigatório");
    }

    public UUID getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public String getBodyShape() {
        return bodyShape;
    }

    public String getKibbeType() {
        return kibbeType;
    }

    public String getColorPalette() {
        return colorPalette;
    }

    public String getZylaPalette() {
        return zylaPalette;
    }

    public String[] getStylePreference() {
        return stylePreference;
    }

    public Map<String, Object> getMeasurements() {
        return measurements;
    }

    public String getAiAnalysisS3Key() {
        return aiAnalysisS3Key;
    }
}
