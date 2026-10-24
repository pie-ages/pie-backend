ALTER TABLE customer ADD COLUMN IF NOT EXISTS supabase_user_id uuid;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_customer_supabase_user') THEN
        ALTER TABLE customer ADD CONSTRAINT uq_customer_supabase_user UNIQUE (supabase_user_id);
    END IF;
END $$;

ALTER TABLE customer DROP COLUMN IF EXISTS password_hash;

ALTER TABLE company ADD COLUMN IF NOT EXISTS supabase_user_id uuid;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_company_supabase_user') THEN
        ALTER TABLE company ADD CONSTRAINT uq_company_supabase_user UNIQUE (supabase_user_id);
    END IF;
END $$;

ALTER TABLE company DROP COLUMN IF EXISTS password_hash;

ALTER TABLE company ADD COLUMN IF NOT EXISTS role   VARCHAR(20) NOT NULL DEFAULT 'COMPANY';
ALTER TABLE company ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

UPDATE company SET status = 'ACTIVE' WHERE status = 'PENDING' AND active = true;
