package com.ages.pie.domain.entity;

import java.util.Objects;

import com.ages.pie.domain.enums.StyleAnswerType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "style_answer")
public class StyleAnswer extends AuditableEntity {

    @EmbeddedId
    private StyleAnswerId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("customerId")
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false)
    private StyleQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = true)
    private StyleOption option;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false)
    private StyleAnswerType answerType;

    protected StyleAnswer() {
    }

    public StyleAnswer(User customer, StyleQuestion question, StyleOption option, StyleAnswerType answerType) {
        this.customer = Objects.requireNonNull(customer, "Cliente é obrigatório");
        this.question = Objects.requireNonNull(question, "Pergunta é obrigatória");
        this.answerType = Objects.requireNonNull(answerType, "Tipo de resposta é obrigatório");
        if (answerType == StyleAnswerType.OPTION) {
            this.option = Objects.requireNonNull(option, "Opção é obrigatória");
            if (this.option.getQuestion() == null
                    || !this.option.getQuestion().getId().equals(question.getId())) {
                throw new IllegalArgumentException("Opção não pertence à pergunta informada");
            }
        } else {
            if (option != null) {
                throw new IllegalArgumentException("Opção deve ser nula para resposta do tipo " + answerType);
            }
            this.option = null;
        }
        this.id = new StyleAnswerId(customer.getId(), question.getId());
    }

    public StyleAnswerId getId() {
        return id;
    }

    public User getCustomer() {
        return customer;
    }

    public StyleQuestion getQuestion() {
        return question;
    }

    public StyleOption getOption() {
        return option;
    }

    public StyleAnswerType getAnswerType() {
        return answerType;
    }
}
