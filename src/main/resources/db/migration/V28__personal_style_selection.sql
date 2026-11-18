-- A seleção manual possui nove estilos. Os valores próprios do quiz de dev
-- continuam válidos nas preferências iniciais, sem aliases ou conversão de dados.
ALTER TABLE customer DROP CONSTRAINT ck_customer_style_preference;
ALTER TABLE customer ADD CONSTRAINT ck_customer_style_preference CHECK (
    style_preference <@ ARRAY[
        'CLASSICO', 'CASUAL', 'CRIATIVO', 'ROMANTICO', 'REFINADO',
        'SENSUAL', 'DRAMATICO', 'MINIMALISTA', 'BOHO',
        'ELEGANTE', 'ESPORTIVO'
    ]::varchar[]
);
