ALTER TABLE look_product ADD COLUMN IF NOT EXISTS display_order integer;
UPDATE look_product SET display_order = 0 WHERE display_order IS NULL;
ALTER TABLE look_product ALTER COLUMN display_order SET NOT NULL;
ALTER TABLE look_product ALTER COLUMN display_order SET DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_look_product_order ON look_product (look_id, display_order);
