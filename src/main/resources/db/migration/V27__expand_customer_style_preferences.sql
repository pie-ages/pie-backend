ALTER TABLE customer
    DROP CONSTRAINT ck_customer_style_preference;

ALTER TABLE customer
    ADD CONSTRAINT ck_customer_style_preference CHECK (
        style_preference <@ ARRAY[
            'CASUAL', 'ELEGANTE', 'ESPORTIVO', 'ROMANTICO', 'MINIMALISTA', 'BOHO',
            'CLASSICO', 'CRIATIVO', 'REFINADO', 'SENSUAL', 'DRAMATICO'
        ]::varchar[]
    );
