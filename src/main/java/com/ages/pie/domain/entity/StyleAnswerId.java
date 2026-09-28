package com.ages.pie.domain.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class StyleAnswerId implements Serializable {

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "question_id")
    private UUID questionId;

    protected StyleAnswerId() {
    }

    public StyleAnswerId(UUID customerId, UUID questionId) {
        this.customerId = Objects.requireNonNull(customerId, "Cliente é obrigatório");
        this.questionId = Objects.requireNonNull(questionId, "Pergunta é obrigatória");
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StyleAnswerId other)) {
            return false;
        }
        return Objects.equals(customerId, other.customerId)
                && Objects.equals(questionId, other.questionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, questionId);
    }
}
