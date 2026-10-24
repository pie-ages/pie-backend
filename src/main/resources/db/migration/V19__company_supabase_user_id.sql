ALTER TABLE company ADD COLUMN supabase_user_id uuid;

ALTER TABLE company
    ADD CONSTRAINT uq_company_supabase_user UNIQUE (supabase_user_id);

ALTER TABLE company DROP COLUMN password_hash;
