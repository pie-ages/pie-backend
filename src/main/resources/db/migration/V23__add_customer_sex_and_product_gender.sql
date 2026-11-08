ALTER TABLE customer
    ADD COLUMN sex varchar(30);

ALTER TABLE customer
    ADD CONSTRAINT ck_customer_sex
        CHECK (sex IN ('MASCULINO', 'FEMININO', 'PREFIRO_NAO_INFORMAR'));

ALTER TABLE product
    ADD COLUMN gender varchar(20);

ALTER TABLE product
    ADD CONSTRAINT ck_product_gender
        CHECK (gender IN ('FEMININO', 'MASCULINO', 'UNISSEX'));