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
    VALUES ('Lucas Mendes', 'lucas.mendes@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_lucas;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Beatriz Costa', 'beatriz.costa@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_beatriz;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Gabriel Souza', 'gabriel.souza@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_gabriel;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Juliana Lima', 'juliana.lima@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_juliana;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Rodrigo Ferreira', 'rodrigo.ferreira@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_rodrigo;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Camila Rocha', 'camila.rocha@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_camila;

    INSERT INTO customer (name, email, password_hash, photo_url, created_at, updated_at)
    VALUES ('Felipe Santos', 'felipe.santos@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_felipe;

    -- companies (marcas reais)
    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('Renner S.A.', '92693249000195', 'https://www.renner.com.br', 'Lojas Renner S.A.', 'Fernanda Oliveira', 'contato@modaestilo.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_modaestilo;

    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('Zara Brasil', '02332886000104', 'https://www.zara.com/br', 'Zara Do Brasil Ltda', 'Carlos Mendes', 'contato@urbanwear.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_urbanwear;

    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('C&A Modas S.A.', '45698435000126', 'https://www.cea.com.br', 'C&A Modas S.A.', 'Mariana Santos', 'contato@elegance.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_elegance;

    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('H&M Brasil', '02773575000180', 'https://www2.hm.com/pt_br/index.html', 'H&M Hennes & Mauritz Comercio Ltda', 'Ana Paula Silva', 'contato@streetculture.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
    RETURNING id INTO v_streetculture;

    INSERT INTO company (name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
    VALUES ('Riachuelo S.A.', '61716327000195', 'https://www.riachuelo.com.br', 'Riachuelo Lojas de Departamento S.A.', 'Roberto Costa', 'contato@basicco.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.pixabay.com%2Fphoto%2F2015%2F10%2F05%2F22%2F37%2Fblank-profile-picture-973460_1280.png&f=1&nofb=1&ipt=ec4b8c78d02124f9b70e073d9679b64c5b6f328ebd108b6cd07432d19ed22501', NOW(), NOW())
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

    -- products (produtos reais das marcas)
    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_modaestilo, 'Blazer Social Feminino', 'Blazer social com corte feminino, tecido de alta qualidade.', 'Superiores', 279.90, 'https://acdn-us.mitiendanube.com/stores/003/859/035/products/sem-titulo-6-e0bab52ee68f0948e817695729129251-640-0.webp', 'https://www.renner.com.br/blazer-social-feminino', true, NOW(), NOW())
    RETURNING id INTO v_prod_blazer;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_modaestilo, 'Calça Jeans Wide Leg Feminina', 'Calça jeans cintura alta com modelagem wide leg.', 'Inferiores', 159.90, 'https://acdn-us.mitiendanube.com/stores/003/859/035/products/sem-titulo-6-e0bab52ee68f0948e817695729129251-640-0.webp', 'https://www.renner.com.br/calca-jeans-wide-leg', true, NOW(), NOW())
    RETURNING id INTO v_prod_jeans;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_urbanwear, 'Jaqueta Oversized Preta', 'Jaqueta oversized em tecido encorpido, estilo urbano.', 'Superiores', 399.90, 'https://adaptive-images.uooucdn.com.br/ik-seo/tr:w-1100,h-1594,c-at_max,pr-true,q-90/a3-ohc9pq/pv/ef/5d/21/e591a6ddca7f8f4856ae47cee2/jaqueta-preta-oversized-80s-large-13.jpg', 'https://www.zara.com/br/jaqueta-oversized-preta', true, NOW(), NOW())
    RETURNING id INTO v_prod_jaqueta;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_elegance, 'Vestido Midi Floral', 'Vestido midi com estampa floral, tecido leve e confortável.', 'Vestidos', 199.90, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fimages.tcdn.com.br%2Fimg%2Fimg_prod%2F954006%2Fvestido_midi_floral_1_20250916185054_ee05becedc76.jpg&f=1&nofb=1&ipt=fcec753af0e6584e7a1b9b52fc80e4419ffda672bd8aac6718e32a59c5996d0d', 'https://www.cea.com.br/vestido-midi-floral', true, NOW(), NOW())
    RETURNING id INTO v_prod_vestido;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_streetculture, 'Camisa Cargo Verde Militar', 'Camisa cargo com bolsos funcionais, estilo casual.', 'Superiores', 149.90, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fimg.lojasrenner.com.br%2Fitem%2F914600871%2Foriginal%2F3.jpg&f=1&nofb=1&ipt=51923144463abeb6d24d01f2afb6f95439992c24d8ff658804f9e470d0e5b2e3', 'https://www2.hm.com/pt_br/productpage.1234567.html', true, NOW(), NOW())
    RETURNING id INTO v_prod_cargo;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_basicco, 'Camiseta Básica Algodão', 'Camiseta básica 100% algodão penteado, diversas cores.', 'Superiores', 49.90, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fimg.lojasrenner.com.br%2Fitem%2F929525130%2Foriginal%2F3.jpg&f=1&nofb=1&ipt=95726fe1a0e17eb589ba144c1aa17d426dafacca7de1bd2575f2ba4247ac892d', 'https://www.riachuelo.com.br/camiseta-basica-algodao', true, NOW(), NOW())
    RETURNING id INTO v_prod_basic;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_modaestilo, 'Saia Midi Plissada', 'Saia midi plissada com elástico na cintura.', 'Inferiores', 129.90, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fdown-br.img.susercontent.com%2Ffile%2Fbr-11134207-7r98o-m6plx45ql0z4f1&f=1&nofb=1&ipt=89aaae0a4f15b40fc3da524dd160eebb299322ea5f2961ef56ae461f178e6bdd', 'https://www.renner.com.br/saia-midi-plissada', true, NOW(), NOW())
    RETURNING id INTO v_prod_saia;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_urbanwear, 'Calça Jogger Moletom', 'Calça jogger em moletom, confortável para o dia a dia.', 'Inferiores', 179.90, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fdown-br.img.susercontent.com%2Ffile%2Fa0d0b827e801a39481b9258f3c7021a6&f=1&nofb=1&ipt=66008c466576f07a10e18990d27ee1a442b4210a3134fac8932c6351f59bb3ef', 'https://www.zara.com/br/calca-jogger-moletom', true, NOW(), NOW())
    RETURNING id INTO v_prod_jogger;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_elegance, 'Trench Coat Clássico', 'Trench coat impermeável, estilo clássico e atemporal.', 'Casacos', 349.90, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fi.pinimg.com%2Foriginals%2F2d%2F7e%2F0c%2F2d7e0cd89fe103bcddf88deecb277d21.jpg&f=1&nofb=1&ipt=38cb654849de3427589d46d31c56c83ba4419d610b6840776a3e6ee4ae2612c0', 'https://www.cea.com.br/trench-coat-classico', true, NOW(), NOW())
    RETURNING id INTO v_prod_trench;

    INSERT INTO product (company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
    VALUES (v_streetculture, 'Moletom Hoodie Estampado', 'Moletom hoodie com estampa gráfica, estilo streetwear.', 'Superiores', 199.90, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fhotmart.s3.amazonaws.com%2Fproduct_pictures%2F02a49336-0ebf-486f-bacf-ac060e46c40f%2FCapturadetela20260103103654.png&f=1&nofb=1&ipt=cc785a74f130b948b9b6f22539b390c013a58e6d15269790236b82d5dcc96113', 'https://www2.hm.com/pt_br/productpage.7654321.html', true, NOW(), NOW())
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
