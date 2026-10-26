-- Posição (0-based) de cada peça dentro do look. A sequência é única por look,
-- compartilhada entre look_wardrobe_item e look_product, para preservar a ordem
-- de adição mesmo quando o usuário intercala peças do guarda-roupa e produtos.

ALTER TABLE look_wardrobe_item ADD COLUMN display_order integer;
ALTER TABLE look_product       ADD COLUMN display_order integer;

-- Backfill: preserva o comportamento atual (created_at; guarda-roupa antes de produto em empate).
WITH unified AS (
    SELECT id, look_id, created_at, 0 AS kind_rank, 'W' AS kind FROM look_wardrobe_item
    UNION ALL
    SELECT id, look_id, created_at, 1 AS kind_rank, 'P' AS kind FROM look_product
),
numbered AS (
    SELECT id, kind,
           ROW_NUMBER() OVER (PARTITION BY look_id ORDER BY created_at, kind_rank, id) - 1 AS pos
    FROM unified
)
UPDATE look_wardrobe_item t SET display_order = n.pos
FROM numbered n WHERE n.kind = 'W' AND n.id = t.id;

WITH unified AS (
    SELECT id, look_id, created_at, 0 AS kind_rank, 'W' AS kind FROM look_wardrobe_item
    UNION ALL
    SELECT id, look_id, created_at, 1 AS kind_rank, 'P' AS kind FROM look_product
),
numbered AS (
    SELECT id, kind,
           ROW_NUMBER() OVER (PARTITION BY look_id ORDER BY created_at, kind_rank, id) - 1 AS pos
    FROM unified
)
UPDATE look_product t SET display_order = n.pos
FROM numbered n WHERE n.kind = 'P' AND n.id = t.id;

ALTER TABLE look_wardrobe_item ALTER COLUMN display_order SET NOT NULL;
ALTER TABLE look_wardrobe_item ALTER COLUMN display_order SET DEFAULT 0;
ALTER TABLE look_product       ALTER COLUMN display_order SET NOT NULL;
ALTER TABLE look_product       ALTER COLUMN display_order SET DEFAULT 0;

CREATE INDEX idx_look_wardrobe_item_order ON look_wardrobe_item (look_id, display_order);
CREATE INDEX idx_look_product_order       ON look_product (look_id, display_order);
