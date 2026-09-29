ALTER TABLE wardrobe_item
    ADD COLUMN name varchar,
    ADD COLUMN style varchar;

UPDATE wardrobe_item wi
SET name = COALESCE(p.name, wi.category, 'Peça')
FROM product p
WHERE wi.product_id = p.id
  AND wi.name IS NULL;

UPDATE wardrobe_item
SET name = COALESCE(category, 'Peça')
WHERE name IS NULL;

ALTER TABLE wardrobe_item
    ALTER COLUMN name SET NOT NULL;