package com.ages.pie.domain.entity;

import java.util.Objects;
import java.util.UUID;

import com.ages.pie.domain.enums.Style;
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
@Table(name = "style_option")
public class StyleOption extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private StyleQuestion question;

    @Column(nullable = false)
    private String label;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Style style;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected StyleOption() {
    }

    public StyleOption(StyleQuestion question, String label, Style style, int displayOrder) {
        this.question = Objects.requireNonNull(question, "Pergunta é obrigatória");
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Rótulo da opção é obrigatório");
        }
        this.label = label;
        this.style = Objects.requireNonNull(style, "Estilo é obrigatório");
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public StyleQuestion getQuestion() {
        return question;
    }

    public String getLabel() {
        return label;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Style getStyle() {
        return style;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
