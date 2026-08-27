-- =============================================================
-- V3 — wishlist, wardrobe_item, look
-- Depende de: customer (V1), product (V2)
--
-- Boas práticas:
-- * wishlist: FK + UNIQUE(customer_id) → 1:1 com customer
-- * wardrobe_item.product_id é nullable — peça pode ser fotografada
--   manualmente (sem vínculo com produto de loja)
-- * look.is_ai_generated distingue look manual de sugestão da IA
-- * DEFERRABLE INITIALLY IMMEDIATE em todas as FKs
-- =============================================================

-- wishlist — 1:1 com customer
CREATE TABLE wishlist (
    id          uuid  PRIMARY KEY,
    customer_id uuid  NOT NULL,
    name        varchar,
    created_at  timestamptz,
    updated_at  timestamptz
);

ALTER TABLE wishlist
    ADD FOREIGN KEY (customer_id) REFERENCES customer(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE wishlist
    ADD CONSTRAINT uq_wishlist_customer UNIQUE (customer_id);

COMMENT ON TABLE wishlist IS 'Lista de desejos do cliente. Relação 1:1 com customer.';

-- wardrobe_item — N:1 com customer; product_id opcional
CREATE TABLE wardrobe_item (
    id          uuid  PRIMARY KEY,
    customer_id uuid  NOT NULL,
    product_id  uuid,
    category    varchar,
    color       varchar,
    photo_url   varchar,
    created_at  timestamptz,
    updated_at  timestamptz
);

ALTER TABLE wardrobe_item
    ADD FOREIGN KEY (customer_id) REFERENCES customer(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE wardrobe_item
    ADD FOREIGN KEY (product_id) REFERENCES product(id) DEFERRABLE INITIALLY IMMEDIATE;

COMMENT ON TABLE  wardrobe_item            IS 'Peças do guarda-roupa do cliente.';
COMMENT ON COLUMN wardrobe_item.product_id IS 'Set when added from a store product; null when manually photographed';

-- look — N:1 com customer
CREATE TABLE look (
    id               uuid     PRIMARY KEY,
    customer_id      uuid     NOT NULL,
    title            varchar,
    is_ai_generated  boolean  DEFAULT false,
    occasion         varchar,
    photo_url        varchar,
    created_at       timestamptz,
    updated_at       timestamptz
);

ALTER TABLE look
    ADD FOREIGN KEY (customer_id) REFERENCES customer(id) DEFERRABLE INITIALLY IMMEDIATE;

COMMENT ON TABLE look IS 'Looks (outfits) montados ou sugeridos pela IA para o cliente.';
