-- -------------------------------------------------------------
-- 1. customer, company
-- -------------------------------------------------------------

CREATE TABLE customer (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    name          varchar,
    email         varchar      UNIQUE,
    password_hash varchar,
    photo_url     varchar,
    created_at    timestamptz,
    updated_at    timestamptz
);

CREATE TABLE company (
    id                 uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    name               varchar,
    cnpj               varchar      UNIQUE,
    website            varchar,
    social_reason      varchar,
    responsible_person varchar,
    email              varchar      UNIQUE,
    password_hash      varchar,
    active             boolean      DEFAULT true,
    photo_url          varchar,
    created_at         timestamptz,
    updated_at         timestamptz
);

-- -------------------------------------------------------------
-- 2. body_profile, product
-- -------------------------------------------------------------

CREATE TABLE body_profile (
    id                  uuid  PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id         uuid  NOT NULL,
    body_shape          varchar,
    kibbe_type          varchar,
    color_palette       varchar,
    zyla_palette        varchar,
    style_preference    varchar[],
    measurements        jsonb,
    ai_analysis_s3_key  varchar,
    created_at          timestamptz,
    updated_at          timestamptz
);

ALTER TABLE body_profile
    ADD FOREIGN KEY (customer_id) REFERENCES customer(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE body_profile
    ADD CONSTRAINT uq_body_profile_customer UNIQUE (customer_id);

CREATE TABLE product (
    id           uuid     PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id   uuid     NOT NULL,
    name         varchar,
    description  text,
    category     varchar,
    price        decimal,
    image_url    varchar,
    purchase_url text,
    active       boolean  DEFAULT true,
    created_at   timestamptz,
    updated_at   timestamptz
);

ALTER TABLE product
    ADD FOREIGN KEY (company_id) REFERENCES company(id) DEFERRABLE INITIALLY IMMEDIATE;

-- -------------------------------------------------------------
-- 3. wishlist, wardrobe_item, look
-- -------------------------------------------------------------

CREATE TABLE wishlist (
    id          uuid  PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id uuid  NOT NULL,
    name        varchar,
    created_at  timestamptz,
    updated_at  timestamptz
);

ALTER TABLE wishlist
    ADD FOREIGN KEY (customer_id) REFERENCES customer(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE wishlist
    ADD CONSTRAINT uq_wishlist_customer UNIQUE (customer_id);

CREATE TABLE wardrobe_item (
    id          uuid  PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id uuid  NOT NULL,
    product_id  uuid,
    category    varchar,
    color       varchar,
    photo_url   varchar,
    created_at  timestamptz,
    updated_at  timestamptz
);

ALTER TABLE wardrobe_item
    ADD FOREIGN KEY (customer_id) REFERENCES customer(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE wardrobe_item
    ADD FOREIGN KEY (product_id) REFERENCES product(id) DEFERRABLE INITIALLY IMMEDIATE;

CREATE TABLE look (
    id               uuid     PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id      uuid     NOT NULL,
    title            varchar,
    is_ai_generated  boolean  DEFAULT false,
    occasion         varchar,
    photo_url        varchar,
    created_at       timestamptz,
    updated_at       timestamptz
);

ALTER TABLE look
    ADD FOREIGN KEY (customer_id) REFERENCES customer(id) DEFERRABLE INITIALLY IMMEDIATE;

-- -------------------------------------------------------------
-- 4. Tabelas de junção
-- -------------------------------------------------------------

CREATE TABLE wishlist_item (
    id          uuid  PRIMARY KEY DEFAULT gen_random_uuid(),
    wishlist_id uuid  NOT NULL,
    product_id  uuid  NOT NULL,
    created_at  timestamptz
);

ALTER TABLE wishlist_item
    ADD FOREIGN KEY (wishlist_id) REFERENCES wishlist(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE wishlist_item
    ADD FOREIGN KEY (product_id) REFERENCES product(id) DEFERRABLE INITIALLY IMMEDIATE;

CREATE UNIQUE INDEX uq_wishlist_product ON wishlist_item (wishlist_id, product_id);

CREATE TABLE look_wardrobe_item (
    id               uuid  PRIMARY KEY DEFAULT gen_random_uuid(),
    look_id          uuid  NOT NULL,
    wardrobe_item_id uuid  NOT NULL,
    created_at       timestamptz
);

ALTER TABLE look_wardrobe_item
    ADD FOREIGN KEY (look_id) REFERENCES look(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE look_wardrobe_item
    ADD FOREIGN KEY (wardrobe_item_id) REFERENCES wardrobe_item(id) DEFERRABLE INITIALLY IMMEDIATE;

CREATE UNIQUE INDEX ON look_wardrobe_item (look_id, wardrobe_item_id);

CREATE TABLE look_product (
    id         uuid  PRIMARY KEY DEFAULT gen_random_uuid(),
    look_id    uuid  NOT NULL,
    product_id uuid  NOT NULL,
    created_at timestamptz
);

ALTER TABLE look_product
    ADD FOREIGN KEY (look_id) REFERENCES look(id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE look_product
    ADD FOREIGN KEY (product_id) REFERENCES product(id) DEFERRABLE INITIALLY IMMEDIATE;

CREATE UNIQUE INDEX ON look_product (look_id, product_id);
