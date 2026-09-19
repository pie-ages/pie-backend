ALTER TABLE product ADD COLUMN status varchar NOT NULL DEFAULT 'RASCUNHO';
-- Backfill: produtos que já estavam ativos e visíveis no catálogo ficam como publicados
UPDATE product SET status = 'PUBLICADO' WHERE active = true;
