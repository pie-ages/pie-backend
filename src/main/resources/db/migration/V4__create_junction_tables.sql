-- =============================================================
-- V4 — wishlist_item, look_wardrobe_item, look_product
-- Depende de: wishlist, wardrobe_item, look, product (V2–V3)
--
-- Boas práticas:
-- * Tabelas de junção têm uuid próprio (flexibilidade para adicionar
--   metadados futuros, ex: quantity, position)
-- * UNIQUE INDEX nas combinações de FK impede duplicatas
--   (mesma peça duas vezes no mesmo look, etc.)
-- * DEFERRABLE INITIALLY IMMEDIATE em todas as FKs
-- =============================================================

-- wishlist_item — junção N:N entre wishlist e product
CREATE TABLE wishlist_item (
    id          uuid  PRIMARY KEY,
    wishlist_id uuid  NOT NULL,
    product_id  uuid  NOT NULL,
    created_at  timestamptz
);

ALTER TABLE wishlist_item
    ADD FOREIGN KEY (wishlist_id) REFERENCES wishlist(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE wishlist_item
    ADD FOREIGN KEY (product_id) REFERENCES product(id) DEFERRABLE INITIALLY IMMEDIATE;

CREATE UNIQUE INDEX uq_wishlist_product ON wishlist_item (wishlist_id, product_id);

COMMENT ON TABLE wishlist_item IS 'Junção N:N entre wishlist e product.';

-- look_wardrobe_item — junção N:N entre look e wardrobe_item
CREATE TABLE look_wardrobe_item (
    id               uuid  PRIMARY KEY,
    look_id          uuid  NOT NULL,
    wardrobe_item_id uuid  NOT NULL,
    created_at       timestamptz
);

ALTER TABLE look_wardrobe_item
    ADD FOREIGN KEY (look_id) REFERENCES look(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE look_wardrobe_item
    ADD FOREIGN KEY (wardrobe_item_id) REFERENCES wardrobe_item(id) DEFERRABLE INITIALLY IMMEDIATE;

CREATE UNIQUE INDEX ON look_wardrobe_item (look_id, wardrobe_item_id);

COMMENT ON TABLE look_wardrobe_item IS 'Junção N:N entre look e wardrobe_item.';

-- look_product — junção N:N entre look e product
CREATE TABLE look_product (
    id         uuid  PRIMARY KEY,
    look_id    uuid  NOT NULL,
    product_id uuid  NOT NULL,
    created_at timestamptz
);

ALTER TABLE look_product
    ADD FOREIGN KEY (look_id) REFERENCES look(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE look_product
    ADD FOREIGN KEY (product_id) REFERENCES product(id) DEFERRABLE INITIALLY IMMEDIATE;

CREATE UNIQUE INDEX ON look_product (look_id, product_id);

COMMENT ON TABLE look_product IS 'Junção N:N entre look e product.';
