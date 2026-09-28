ALTER TABLE customer
    ADD COLUMN style_preference varchar[] NOT NULL DEFAULT '{}'::varchar[];

UPDATE customer
SET style_preference = style_result
WHERE style_result IS NOT NULL
    AND style_result <@ ARRAY['CASUAL', 'ELEGANTE', 'ESPORTIVO', 'ROMANTICO', 'MINIMALISTA', 'BOHO']::varchar[];

ALTER TABLE customer
    ADD CONSTRAINT ck_customer_style_preference CHECK (
        style_preference <@ ARRAY['CASUAL', 'ELEGANTE', 'ESPORTIVO', 'ROMANTICO', 'MINIMALISTA', 'BOHO']::varchar[]
    );

ALTER TABLE style_answer
    ADD CONSTRAINT ck_style_answer_option_type CHECK (
        (answer_type = 'OPTION' AND option_id IS NOT NULL) OR
        (answer_type IN ('BOTH', 'NONE') AND option_id IS NULL)
    );
    