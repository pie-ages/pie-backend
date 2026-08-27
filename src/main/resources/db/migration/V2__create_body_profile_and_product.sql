-- =============================================================
-- V2 — body_profile, product
-- Depende de: customer, company (V1)
--
-- Boas práticas:
-- * body_profile: FK + UNIQUE(customer_id) → relação 1:1 com customer
-- * style_preference varchar[] → array nativo do PostgreSQL
-- * measurements jsonb → estrutura flexível sem schema fixo
-- * product.price decimal com COMMENT de não-negativo
-- * product.active boolean → desativa produto sem deletar
-- * DEFERRABLE INITIALLY IMMEDIATE → FK verificada por statement
--   (pode ser adiada dentro de uma transação se necessário)
-- =============================================================

-- body_profile — 1:1 com customer
CREATE TABLE body_profile (
    id                  uuid  PRIMARY KEY,
    customer_id         uuid  NOT NULL,
    body_shape          varchar,
    kibbe_type          varchar,
    color_palette       varchar,
    zyla_palette        varchar,
    style_preference    varchar[],
    measurements        jsonb,
    ai_analysis_s3_key  varchar,
    created_at          timestamptz,
    updated_at          timestamptz
);

ALTER TABLE body_profile
    ADD FOREIGN KEY (customer_id) REFERENCES customer(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE body_profile
    ADD CONSTRAINT uq_body_profile_customer UNIQUE (customer_id);

COMMENT ON TABLE body_profile IS 'Perfil corporal do cliente. Relação 1:1 com customer.';

-- product — N:1 com company (produto vai direto na empresa, sem storefront)
CREATE TABLE product (
    id           uuid     PRIMARY KEY,
    company_id   uuid     NOT NULL,
    name         varchar,
    description  text,
    category     varchar,
    price        decimal,
    image_url    varchar,
    purchase_url text,
    active       boolean  DEFAULT true,
    created_at   timestamptz,
    updated_at   timestamptz
);

ALTER TABLE product
    ADD FOREIGN KEY (company_id) REFERENCES company(id) DEFERRABLE INITIALLY IMMEDIATE;

COMMENT ON TABLE  product       IS 'Produtos disponíveis nas empresas da plataforma PIE.';
COMMENT ON COLUMN product.price IS 'Should be non-negative';
