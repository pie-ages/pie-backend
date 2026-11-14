ALTER TABLE look_wardrobe_item ADD COLUMN IF NOT EXISTS display_order integer;
UPDATE look_wardrobe_item SET display_order = 0 WHERE display_order IS NULL;
ALTER TABLE look_wardrobe_item ALTER COLUMN display_order SET NOT NULL;
ALTER TABLE look_wardrobe_item ALTER COLUMN display_order SET DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_look_wardrobe_item_order ON look_wardrobe_item (look_id, display_order);
