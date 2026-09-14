ALTER TABLE product ADD COLUMN available boolean DEFAULT true NOT NULL;
-- Backfill: produtos ativos continuam disponíveis, inativos ficam indisponíveis
UPDATE product SET available = active;
