ALTER TABLE company ADD COLUMN IF NOT EXISTS supabase_user_id uuid;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_company_supabase_user') THEN
        ALTER TABLE company ADD CONSTRAINT uq_company_supabase_user UNIQUE (supabase_user_id);
    END IF;
END $$;

ALTER TABLE company DROP COLUMN IF EXISTS password_hash;
