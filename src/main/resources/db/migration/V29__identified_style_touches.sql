ALTER TABLE body_profile
    ADD COLUMN identified_style_touches varchar[] NOT NULL DEFAULT '{}'::varchar[];

ALTER TABLE customer ADD CONSTRAINT ck_customer_style_result CHECK (
    style_result IS NULL OR style_result <@ ARRAY['CASUAL', 'ELEGANTE', 'ESPORTIVO', 'ROMANTICO', 'MINIMALISTA', 'BOHO']::varchar[]
);
ALTER TABLE body_profile ADD CONSTRAINT ck_body_profile_identified_style CHECK (
    identified_style IS NULL OR identified_style IN ('CASUAL', 'ELEGANTE', 'ESPORTIVO', 'ROMANTICO', 'MINIMALISTA', 'BOHO')
);
ALTER TABLE body_profile ADD CONSTRAINT ck_body_profile_identified_style_touches CHECK (
    identified_style_touches <@ ARRAY['CASUAL', 'ELEGANTE', 'ESPORTIVO', 'ROMANTICO', 'MINIMALISTA', 'BOHO']::varchar[]
);
