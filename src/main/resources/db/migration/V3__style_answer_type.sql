ALTER TABLE style_answer
    ALTER COLUMN option_id DROP NOT NULL;

ALTER TABLE style_answer
    ADD COLUMN answer_type varchar NOT NULL DEFAULT 'OPTION';

ALTER TABLE style_answer
    ALTER COLUMN answer_type DROP DEFAULT;
