ALTER TABLE customer ADD COLUMN supabase_user_id uuid;

ALTER TABLE customer
    ADD CONSTRAINT uq_customer_supabase_user UNIQUE (supabase_user_id);

ALTER TABLE customer DROP COLUMN password_hash;
