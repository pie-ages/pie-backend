-- -------------------------------------------------------------
-- 1. CUSTOMER & COMPANY
-- -------------------------------------------------------------

DO $$
DECLARE
    v_ana uuid;          v_carlos uuid;      v_mariana uuid;
    v_lucas uuid;        v_beatriz uuid;     v_gabriel uuid;
    v_juliana uuid;      v_rodrigo uuid;     v_camila uuid;
    v_felipe uuid;

    v_modaestilo uuid;   v_urbanwear uuid;   v_elegance uuid;
    v_streetculture uuid; v_basicco uuid;

    v_bp_ana uuid;       v_bp_carlos uuid;   v_bp_mariana uuid;
    v_bp_lucas uuid;     v_bp_beatriz uuid;  v_bp_gabriel uuid;
    v_bp_juliana uuid;   v_bp_rodrigo uuid;  v_bp_camila uuid;
    v_bp_felipe uuid;

    v_prod_blazer uuid;  v_prod_jeans uuid;  v_prod_jaqueta uuid;
    v_prod_vestido uuid; v_prod_cargo uuid;  v_prod_basic uuid;
    v_prod_saia uuid;    v_prod_jogger uuid; v_prod_trench uuid;
    v_prod_hoodie uuid;

    v_wl_ana uuid;       v_wl_carlos uuid;   v_wl_mariana uuid;
    v_wl_lucas uuid;     v_wl_beatriz uuid;  v_wl_gabriel uuid;
    v_wl_juliana uuid;   v_wl_rodrigo uuid;  v_wl_camila uuid;
    v_wl_felipe uuid;

    v_wi_blazer uuid;    v_wi_tshirt uuid;   v_wi_jacket uuid;
    v_wi_dress uuid;     v_wi_shirt uuid;    v_wi_basic uuid;
    v_wi_jogger uuid;    v_wi_skirt uuid;    v_wi_hoodie uuid;
    v_wi_coat uuid;

    v_look_trabalho uuid;    v_look_outing uuid;
    v_look_jantar uuid;      v_look_street uuid;
    v_look_aerolook uuid;    v_look_treino uuid;
    v_look_reuniao uuid;     v_look_show uuid;
    v_look_almoco uuid;      v_look_minimalist uuid;
BEGIN
    -- customers
    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Ana Silva', 'ana.silva@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/ana.jpg', NOW(), NOW())
    RETURNING id INTO v_ana;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Carlos Eduardo', 'carlos.eduardo@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/carlos.jpg', NOW(), NOW())
    RETURNING id INTO v_carlos;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Mariana Oliveira', 'mariana.oliveira@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/mariana.jpg', NOW(), NOW())
    RETURNING id INTO v_mariana;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Lucas Mendes', 'lucas.mendes@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/lucas.jpg', NOW(), NOW())
    RETURNING id INTO v_lucas;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Beatriz Costa', 'beatriz.costa@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/beatriz.jpg', NOW(), NOW())
    RETURNING id INTO v_beatriz;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Gabriel Souza', 'gabriel.souza@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/gabriel.jpg', NOW(), NOW())
    RETURNING id INTO v_gabriel;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Juliana Lima', 'juliana.lima@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/juliana.jpg', NOW(), NOW())
    RETURNING id INTO v_juliana;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Rodrigo Ferreira', 'rodrigo.ferreira@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/rodrigo.jpg', NOW(), NOW())
    RETURNING id INTO v_rodrigo;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Camila Rocha', 'camila.rocha@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/camila.jpg', NOW(), NOW())
    RETURNING id INTO v_camila;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Felipe Santos', 'felipe.santos@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/felipe.jpg', NOW(), NOW())
    RETURNING id INTO v_felipe;

    -- companies
    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('Moda & Estilo Ltda', '12345678000195', 'https://modaestilo.example.com', 'Moda & Estilo Comercio S.A.', 'Mariana Costa', 'contato@modaestilo.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/modaestilo.png', NOW(), NOW())
    RETURNING id INTO v_modaestilo;

    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('Urban Wear', '98765432000110', 'https://urbanwear.example.com', 'Urban Wear Confeccoes Ltda', 'Roberto Alves', 'contato@urbanwear.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/urbanwear.png', NOW(), NOW())
    RETURNING id INTO v_urbanwear;

    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('Elegance Concept', '11223344000155', 'https://elegance.example.com', 'Elegance Vestuario S.A.', 'Patricia Abravanel', 'contato@elegance.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/elegance.png', NOW(), NOW())
    RETURNING id INTO v_elegance;

    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('Street Culture', '55667788000199', 'https://streetculture.example.com', 'Street Culture Moda Urbana', 'Thiago Silva', 'contato@streetculture.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/streetculture.png', NOW(), NOW())
    RETURNING id INTO v_streetculture;

    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('Basic & Co', '99887766000122', 'https://basicco.example.com', 'Basic & Co Atacado e Varejo', 'Vanessa Camargo', 'contato@basicco.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/basicco.png', NOW(), NOW())
    RETURNING id INTO v_basicco;

    -- -------------------------------------------------------------
    -- 2. BODY_PROFILE & PRODUCT
    -- -------------------------------------------------------------

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_ana, 'Hourglass', 'Soft Dramatic', 'Autumn Warm', 'Sunset Bronze', ARRAY['Casual', 'Elegante'], '{"bust": 90, "waist": 68, "hips": 96}'::jsonb, 'analyses/ana.json', NOW(), NOW())
    RETURNING id INTO v_bp_ana;

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_carlos, 'Rectangle', 'Flamboyant Natural', 'Winter Deep', 'Classic Navy', ARRAY['Sporty', 'Streetwear'], '{"chest": 102, "waist": 84}'::jsonb, 'analyses/carlos.json', NOW(), NOW())
    RETURNING id INTO v_bp_carlos;

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_mariana, 'Pear', 'Romantic', 'Spring Light', 'Rose Coral', ARRAY['Feminino', 'Vintage'], '{"bust": 85, "waist": 65, "hips": 98}'::jsonb, 'analyses/mariana.json', NOW(), NOW())
    RETURNING id INTO v_bp_mariana;

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_lucas, 'Inverted Triangle', 'Dramatic Classic', 'Summer Cool', 'Slate Grey', ARRAY['Modern', 'Minimalist'], '{"chest": 110, "waist": 88}'::jsonb, 'analyses/lucas.json', NOW(), NOW())
    RETURNING id INTO v_bp_lucas;

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_beatriz, 'Hourglass', 'Theatrical Romantic', 'Autumn Dark', 'Earthy Ochre', ARRAY['Boho', 'Chic'], '{"bust": 92, "waist": 70, "hips": 95}'::jsonb, 'analyses/beatriz.json', NOW(), NOW())
    RETURNING id INTO v_bp_beatriz;

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_gabriel, 'Trapezoid', 'Natural', 'Spring Bright', 'Emerald Green', ARRAY['Casual', 'Sporty'], '{"chest": 98, "waist": 80}'::jsonb, 'analyses/gabriel.json', NOW(), NOW())
    RETURNING id INTO v_bp_gabriel;

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_juliana, 'Apple', 'Soft Classic', 'Summer Light', 'Soft Lavender', ARRAY['Executivo', 'Elegante'], '{"bust": 100, "waist": 88, "hips": 102}'::jsonb, 'analyses/juliana.json', NOW(), NOW())
    RETURNING id INTO v_bp_juliana;

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_rodrigo, 'Rectangle', 'Gamine', 'Winter Clear', 'Midnight Blue', ARRAY['Urban', 'Rock'], '{"chest": 94, "waist": 78}'::jsonb, 'analyses/rodrigo.json', NOW(), NOW())
    RETURNING id INTO v_bp_rodrigo;

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_camila, 'Pear', 'Soft Natural', 'Autumn Soft', 'Warm Olive', ARRAY['Casual', 'Comfort'], '{"bust": 88, "waist": 72, "hips": 104}'::jsonb, 'analyses/camila.json', NOW(), NOW())
    RETURNING id INTO v_bp_camila;

    INSERT INTO body_profile (customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
    VALUES (v_felipe, 'Inverted Triangle', 'Dramatic', 'Winter Deep', 'Charcoal', ARRAY['Minimalist', 'Formal'], '{"chest": 106, "waist": 82}'::jsonb, 'analyses/felipe.json', NOW(), NOW())
    RETURNING id INTO v_bp_felipe;

    -- products
    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_modaestilo, 'Blazer Alfaiataria Bege', 'Blazer de alfaiataria estruturado com caimento moderno.', 'Superiores', 299.90, 'https://example.com/p/blazer-bege.jpg', 'https://modaestilo.example.com/p/1', true, NOW(), NOW())
    RETURNING id INTO v_prod_blazer;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_modaestilo, 'Calca Jeans Wide Leg', 'Calca jeans cintura alta com modelagem ampla.', 'Inferiores', 189.90, 'https://example.com/p/jeans-wide.jpg', 'https://modaestilo.example.com/p/2', true, NOW(), NOW())
    RETURNING id INTO v_prod_jeans;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_urbanwear, 'Jaqueta Oversized Preto', 'Jaqueta de sarja preta estilo urbano.', 'Superiores', 249.00, 'https://example.com/p/jaqueta-preta.jpg', 'https://urbanwear.example.com/p/3', true, NOW(), NOW())
    RETURNING id INTO v_prod_jaqueta;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_elegance, 'Vestido Mid Silk Red', 'Vestido midi em seda pura na cor vermelha.', 'Vestidos', 450.00, 'https://example.com/p/vestido-seda.jpg', 'https://elegance.example.com/p/4', true, NOW(), NOW())
    RETURNING id INTO v_prod_vestido;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_streetculture, 'Camisa Cargo Utility Green', 'Camisa estilo cargo com bolsos frontais.', 'Superiores', 179.90, 'https://example.com/p/camisa-cargo.jpg', 'https://streetculture.example.com/p/5', true, NOW(), NOW())
    RETURNING id INTO v_prod_cargo;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_basicco, 'Camiseta Basic Cotton White', 'Camiseta 100% algodao penteado.', 'Superiores', 79.90, 'https://example.com/p/tshirt-white.jpg', 'https://basicco.example.com/p/6', true, NOW(), NOW())
    RETURNING id INTO v_prod_basic;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_modaestilo, 'Saia Midi Plissada Preto', 'Saia plissada com elastico na cintura.', 'Inferiores', 159.90, 'https://example.com/p/saia-plissada.jpg', 'https://modaestilo.example.com/p/7', true, NOW(), NOW())
    RETURNING id INTO v_prod_saia;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_urbanwear, 'Calca Jogger Moletom Grey', 'Calca jogger confortavel em algodao.', 'Inferiores', 139.90, 'https://example.com/p/jogger-grey.jpg', 'https://urbanwear.example.com/p/8', true, NOW(), NOW())
    RETURNING id INTO v_prod_jogger;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_elegance, 'Trench Coat Classico', 'Casaco impermeavel classico para inverno.', 'Casacos', 599.00, 'https://example.com/p/trench-coat.jpg', 'https://elegance.example.com/p/9', true, NOW(), NOW())
    RETURNING id INTO v_prod_trench;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_streetculture, 'Moletom Hoodie Graphic', 'Hoodie estampado com estampa urbana.', 'Superiores', 219.90, 'https://example.com/p/hoodie-graphic.jpg', 'https://streetculture.example.com/p/10', true, NOW(), NOW())
    RETURNING id INTO v_prod_hoodie;

    -- -------------------------------------------------------------
    -- 3. WISHLIST, WARDROBE_ITEM & LOOK
    -- -------------------------------------------------------------

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_ana, 'Favoritos de Outono', NOW(), NOW())
    RETURNING id INTO v_wl_ana;

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_carlos, 'Streetwear Wishlist', NOW(), NOW())
    RETURNING id INTO v_wl_carlos;

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_mariana, 'Looks de Festas', NOW(), NOW())
    RETURNING id INTO v_wl_mariana;

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_lucas, 'Inverno 2026', NOW(), NOW())
    RETURNING id INTO v_wl_lucas;

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_beatriz, 'Viagem Europa', NOW(), NOW())
    RETURNING id INTO v_wl_beatriz;

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_gabriel, 'Academia & Esporte', NOW(), NOW())
    RETURNING id INTO v_wl_gabriel;

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_juliana, 'Roupas de Trabalho', NOW(), NOW())
    RETURNING id INTO v_wl_juliana;

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_rodrigo, 'Estilo Minimalista', NOW(), NOW())
    RETURNING id INTO v_wl_rodrigo;

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_camila, 'Looks de Primavera', NOW(), NOW())
    RETURNING id INTO v_wl_camila;

    INSERT INTO wishlist (customer_id, name, created_at, updated_at)
    VALUES (v_felipe, 'Basicos Essenciais', NOW(), NOW())
    RETURNING id INTO v_wl_felipe;

    -- wardrobe_items
    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_ana, v_prod_blazer, 'Casacos', 'Bege', 'https://example.com/w/blazer.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_blazer;

    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_ana, NULL, 'Camisetas', 'Branco', 'https://example.com/w/tshirt.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_tshirt;

    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_carlos, v_prod_jaqueta, 'Casacos', 'Preto', 'https://example.com/w/jacket.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_jacket;

    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_mariana, v_prod_vestido, 'Vestidos', 'Vermelho', 'https://example.com/w/dress.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_dress;

    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_lucas, v_prod_cargo, 'Camisas', 'Verde', 'https://example.com/w/shirt.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_shirt;

    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_beatriz, v_prod_basic, 'Camisetas', 'Branco', 'https://example.com/w/basic.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_basic;

    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_gabriel, v_prod_jogger, 'Calcac', 'Cinza', 'https://example.com/w/jogger.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_jogger;

    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_juliana, v_prod_saia, 'Saias', 'Preto', 'https://example.com/w/skirt.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_skirt;

    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_rodrigo, v_prod_hoodie, 'Moletom', 'Preto', 'https://example.com/w/hoodie.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_hoodie;

    INSERT INTO wardrobe_item (customer_id, product_id, category, color, photo_url, created_at, updated_at)
    VALUES (v_camila, v_prod_trench, 'Casacos', 'Bege', 'https://example.com/w/coat.jpg', NOW(), NOW())
    RETURNING id INTO v_wi_coat;

    -- looks
    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_ana, 'Look Trabalho Elegante', true, 'Trabalho', 'https://example.com/l/1.jpg', NOW(), NOW())
    RETURNING id INTO v_look_trabalho;

    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_carlos, 'Outing Casual Fim de Semana', false, 'Lazer', 'https://example.com/l/2.jpg', NOW(), NOW())
    RETURNING id INTO v_look_outing;

    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_mariana, 'Jantar Romantico', true, 'Jantar', 'https://example.com/l/3.jpg', NOW(), NOW())
    RETURNING id INTO v_look_jantar;

    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_lucas, 'Streetwear Casual', false, 'Passeio', 'https://example.com/l/4.jpg', NOW(), NOW())
    RETURNING id INTO v_look_street;

    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_beatriz, 'Aerolook Confortavel', true, 'Viagem', 'https://example.com/l/5.jpg', NOW(), NOW())
    RETURNING id INTO v_look_aerolook;

    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_gabriel, 'Treino Esportivo', false, 'Academia', 'https://example.com/l/6.jpg', NOW(), NOW())
    RETURNING id INTO v_look_treino;

    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_juliana, 'Reuniao Executiva', true, 'Reuniao', 'https://example.com/l/7.jpg', NOW(), NOW())
    RETURNING id INTO v_look_reuniao;

    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_rodrigo, 'Show / Evento Noturno', false, 'Festa', 'https://example.com/l/8.jpg', NOW(), NOW())
    RETURNING id INTO v_look_show;

    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_camila, 'Almoco em Familia', true, 'Lazer', 'https://example.com/l/9.jpg', NOW(), NOW())
    RETURNING id INTO v_look_almoco;

    INSERT INTO look (customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
    VALUES (v_felipe, 'Minimalist Everyday', false, 'Diario', 'https://example.com/l/10.jpg', NOW(), NOW())
    RETURNING id INTO v_look_minimalist;

    -- -------------------------------------------------------------
    -- 4. TABELAS DE JUNCAO
    -- -------------------------------------------------------------

    -- wishlist_items
    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_ana, v_prod_blazer, NOW());

    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_ana, v_prod_jeans, NOW());

    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_carlos, v_prod_jaqueta, NOW());

    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_mariana, v_prod_vestido, NOW());

    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_lucas, v_prod_trench, NOW());

    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_beatriz, v_prod_cargo, NOW());

    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_gabriel, v_prod_jogger, NOW());

    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_juliana, v_prod_saia, NOW());

    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_rodrigo, v_prod_basic, NOW());

    INSERT INTO wishlist_item (wishlist_id, product_id, created_at)
    VALUES (v_wl_camila, v_prod_hoodie, NOW());

    -- look_wardrobe_items
    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_trabalho, v_wi_blazer, NOW());

    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_trabalho, v_wi_tshirt, NOW());

    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_outing, v_wi_jacket, NOW());

    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_jantar, v_wi_dress, NOW());

    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_street, v_wi_shirt, NOW());

    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_aerolook, v_wi_basic, NOW());

    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_treino, v_wi_jogger, NOW());

    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_reuniao, v_wi_skirt, NOW());

    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_show, v_wi_hoodie, NOW());

    INSERT INTO look_wardrobe_item (look_id, wardrobe_item_id, created_at)
    VALUES (v_look_almoco, v_wi_coat, NOW());

    -- look_products
    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_trabalho, v_prod_jeans, NOW());

    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_outing, v_prod_jaqueta, NOW());

    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_jantar, v_prod_vestido, NOW());

    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_street, v_prod_cargo, NOW());

    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_aerolook, v_prod_basic, NOW());

    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_treino, v_prod_jogger, NOW());

    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_reuniao, v_prod_saia, NOW());

    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_show, v_prod_hoodie, NOW());

    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_almoco, v_prod_trench, NOW());

    INSERT INTO look_product (look_id, product_id, created_at)
    VALUES (v_look_minimalist, v_prod_blazer, NOW());
END $$;
