ALTER TABLE customer ADD COLUMN IF NOT EXISTS supabase_user_id uuid;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_customer_supabase_user') THEN
        ALTER TABLE customer ADD CONSTRAINT uq_customer_supabase_user UNIQUE (supabase_user_id);
    END IF;
END $$;

ALTER TABLE customer DROP COLUMN IF EXISTS password_hash;
