package com.ages.pie.domain.entity;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "style_question")
public class StyleQuestion extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "question_text", nullable = false)
    private String text;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;

    protected StyleQuestion() {
    }

    public StyleQuestion(String text, int displayOrder) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Texto da pergunta é obrigatório");
        }
        this.text = text;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setText(String text) {
        this.text = Objects.requireNonNull(text, "Texto da pergunta é obrigatório");
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
