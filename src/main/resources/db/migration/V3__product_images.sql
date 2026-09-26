CREATE TABLE product_image (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id    UUID        NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    url           VARCHAR     NOT NULL,
    storage_key   VARCHAR     NOT NULL,
    is_primary    BOOLEAN     NOT NULL DEFAULT false,
    display_order INTEGER     NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ,
    updated_at    TIMESTAMPTZ
);

CREATE INDEX idx_product_image_product_id ON product_image(product_id);
