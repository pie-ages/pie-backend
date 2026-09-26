-- -------------------------------------------------------------
-- PIE-58 (US09): questionário de estilo — schema
-- -------------------------------------------------------------

CREATE TABLE style_question (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    question_text varchar      NOT NULL,
    display_order integer      NOT NULL,
    active        boolean      NOT NULL DEFAULT true,
    created_at    timestamptz,
    updated_at    timestamptz
);

CREATE TABLE style_option (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id   uuid         NOT NULL,
    label         varchar      NOT NULL,
    image_url     varchar,
    style         varchar      NOT NULL,
    display_order integer      NOT NULL,
    created_at    timestamptz,
    updated_at    timestamptz
);

ALTER TABLE style_option
    ADD FOREIGN KEY (question_id) REFERENCES style_question(id) DEFERRABLE INITIALLY IMMEDIATE;

CREATE TABLE style_answer (
    customer_id   uuid         NOT NULL,
    question_id   uuid         NOT NULL,
    option_id     uuid         NOT NULL,
    created_at    timestamptz,
    updated_at    timestamptz,
    PRIMARY KEY (customer_id, question_id)
);

ALTER TABLE style_answer
    ADD FOREIGN KEY (customer_id) REFERENCES customer(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE style_answer
    ADD FOREIGN KEY (question_id) REFERENCES style_question(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE style_answer
    ADD FOREIGN KEY (option_id) REFERENCES style_option(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE customer
    ADD COLUMN style_result varchar;
