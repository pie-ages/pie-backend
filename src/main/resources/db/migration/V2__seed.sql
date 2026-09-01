-- =============================================================
-- SEED DATA EXTENDED FOR FLYWAY (PostgreSQL)
-- =============================================================

-- -------------------------------------------------------------
-- 1. CUSTOMER & COMPANY
-- -------------------------------------------------------------

INSERT INTO customer (id, name, email, password_hash, photo_url, created_at, updated_at)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Ana Silva', 'ana.silva@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/ana.jpg', NOW(), NOW()),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Carlos Eduardo', 'carlos.eduardo@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/carlos.jpg', NOW(), NOW()),
    ('a3eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'Mariana Oliveira', 'mariana.oliveira@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/mariana.jpg', NOW(), NOW()),
    ('a4eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'Lucas Mendes', 'lucas.mendes@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/lucas.jpg', NOW(), NOW()),
    ('a5eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'Beatriz Costa', 'beatriz.costa@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/beatriz.jpg', NOW(), NOW()),
    ('a6eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'Gabriel Souza', 'gabriel.souza@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/gabriel.jpg', NOW(), NOW()),
    ('a7eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'Juliana Lima', 'juliana.lima@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/juliana.jpg', NOW(), NOW()),
    ('a8eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'Rodrigo Ferreira', 'rodrigo.ferreira@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/rodrigo.jpg', NOW(), NOW()),
    ('a9eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'Camila Rocha', 'camila.rocha@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/camila.jpg', NOW(), NOW()),
    ('a10ebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 'Felipe Santos', 'felipe.santos@example.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', 'https://example.com/photos/felipe.jpg', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

INSERT INTO company (id, name, cnpj, website, social_reason, responsible_person, email, password_hash, active, photo_url, created_at, updated_at)
VALUES
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Moda & Estilo Ltda', '12345678000195', 'https://modaestilo.example.com', 'Moda & Estilo Comércio S.A.', 'Mariana Costa', 'contato@modaestilo.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/modaestilo.png', NOW(), NOW()),
    ('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Urban Wear', '98765432000110', 'https://urbanwear.example.com', 'Urban Wear Confecções Ltda', 'Roberto Alves', 'contato@urbanwear.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/urbanwear.png', NOW(), NOW()),
    ('c3eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'Elegance Concept', '11223344000155', 'https://elegance.example.com', 'Elegance Vestuário S.A.', 'Patricia Abravanel', 'contato@elegance.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/elegance.png', NOW(), NOW()),
    ('c4eebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'Street Culture', '55667788000199', 'https://streetculture.example.com', 'Street Culture Moda Urbana', 'Thiago Silva', 'contato@streetculture.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/streetculture.png', NOW(), NOW()),
    ('c5eebc99-9c0b-4ef8-bb6d-6bb9bd380c05', 'Basic & Co', '99887766000122', 'https://basicco.example.com', 'Basic & Co Atacado e Varejo', 'Vanessa Camargo', 'contato@basicco.com', '$2a$12$eImiTXuWVxfM37uY4JANjOL.80F80.0123456789abcdefghij', true, 'https://example.com/brands/basicco.png', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- -------------------------------------------------------------
-- 2. BODY_PROFILE & PRODUCT
-- -------------------------------------------------------------

INSERT INTO body_profile (id, customer_id, body_shape, kibbe_type, color_palette, zyla_palette, style_preference, measurements, ai_analysis_s3_key, created_at, updated_at)
VALUES
    ('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Hourglass', 'Soft Dramatic', 'Autumn Warm', 'Sunset Bronze', ARRAY['Casual', 'Elegante'], '{"bust": 90, "waist": 68, "hips": 96}'::jsonb, 'analyses/ana.json', NOW(), NOW()),
    ('f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Rectangle', 'Flamboyant Natural', 'Winter Deep', 'Classic Navy', ARRAY['Sporty', 'Streetwear'], '{"chest": 102, "waist": 84}'::jsonb, 'analyses/carlos.json', NOW(), NOW()),
    ('b3eebc99-9c0b-4ef8-bb6d-6bb9bd380b03', 'a3eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'Pear', 'Romantic', 'Spring Light', 'Rose Coral', ARRAY['Feminino', 'Vintage'], '{"bust": 85, "waist": 65, "hips": 98}'::jsonb, 'analyses/mariana.json', NOW(), NOW()),
    ('b4eebc99-9c0b-4ef8-bb6d-6bb9bd380b04', 'a4eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'Inverted Triangle', 'Dramatic Classic', 'Summer Cool', 'Slate Grey', ARRAY['Modern', 'Minimalist'], '{"chest": 110, "waist": 88}'::jsonb, 'analyses/lucas.json', NOW(), NOW()),
    ('b5eebc99-9c0b-4ef8-bb6d-6bb9bd380b05', 'a5eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'Hourglass', 'Theatrical Romantic', 'Autumn Dark', 'Earthy Ochre', ARRAY['Boho', 'Chic'], '{"bust": 92, "waist": 70, "hips": 95}'::jsonb, 'analyses/beatriz.json', NOW(), NOW()),
    ('b6eebc99-9c0b-4ef8-bb6d-6bb9bd380b06', 'a6eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'Trapezoid', 'Natural', 'Spring Bright', 'Emerald Green', ARRAY['Casual', 'Sporty'], '{"chest": 98, "waist": 80}'::jsonb, 'analyses/gabriel.json', NOW(), NOW()),
    ('b7eebc99-9c0b-4ef8-bb6d-6bb9bd380b07', 'a7eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'Apple', 'Soft Classic', 'Summer Light', 'Soft Lavender', ARRAY['Executivo', 'Elegante'], '{"bust": 100, "waist": 88, "hips": 102}'::jsonb, 'analyses/juliana.json', NOW(), NOW()),
    ('b8eebc99-9c0b-4ef8-bb6d-6bb9bd380b08', 'a8eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'Rectangle', 'Gamine', 'Winter Clear', 'Midnight Blue', ARRAY['Urban', 'Rock'], '{"chest": 94, "waist": 78}'::jsonb, 'analyses/rodrigo.json', NOW(), NOW()),
    ('b9eebc99-9c0b-4ef8-bb6d-6bb9bd380b09', 'a9eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'Pear', 'Soft Natural', 'Autumn Soft', 'Warm Olive', ARRAY['Casual', 'Comfort'], '{"bust": 88, "waist": 72, "hips": 104}'::jsonb, 'analyses/camila.json', NOW(), NOW()),
    ('b10ebc99-9c0b-4ef8-bb6d-6bb9bd380b10', 'a10ebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 'Inverted Triangle', 'Dramatic', 'Winter Deep', 'Charcoal', ARRAY['Minimalist', 'Formal'], '{"chest": 106, "waist": 82}'::jsonb, 'analyses/felipe.json', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

INSERT INTO product (id, company_id, name, description, category, price, image_url, purchase_url, active, created_at, updated_at)
VALUES
    ('11eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Blazer Alfaiataria Bege', 'Blazer de alfaiataria estruturado com caimento moderno.', 'Superiores', 299.90, 'https://example.com/p/blazer-bege.jpg', 'https://modaestilo.example.com/p/1', true, NOW(), NOW()),
    ('22eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Calça Jeans Wide Leg', 'Calça jeans cintura alta com modelagem ampla.', 'Inferiores', 189.90, 'https://example.com/p/jeans-wide.jpg', 'https://modaestilo.example.com/p/2', true, NOW(), NOW()),
    ('33eebc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'd3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Jaqueta Oversized Preto', 'Jaqueta de sarja preta estilo urbano.', 'Superiores', 249.00, 'https://example.com/p/jaqueta-preta.jpg', 'https://urbanwear.example.com/p/3', true, NOW(), NOW()),
    ('a04ebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'c3eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'Vestido Mid Silk Red', 'Vestido midi em seda pura na cor vermelha.', 'Vestidos', 450.00, 'https://example.com/p/vestido-seda.jpg', 'https://elegance.example.com/p/4', true, NOW(), NOW()),
    ('a05ebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'c4eebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'Camisa Cargo Utility Green', 'Camisa estilo cargo com bolsos frontais.', 'Superiores', 179.90, 'https://example.com/p/camisa-cargo.jpg', 'https://streetculture.example.com/p/5', true, NOW(), NOW()),
    ('a06ebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'c5eebc99-9c0b-4ef8-bb6d-6bb9bd380c05', 'Camiseta Basic Cotton White', 'Camiseta 100% algodão penteado.', 'Superiores', 79.90, 'https://example.com/p/tshirt-white.jpg', 'https://basicco.example.com/p/6', true, NOW(), NOW()),
    ('a07ebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Saia Midi Plissada Preto', 'Saia plissada com elástico na cintura.', 'Inferiores', 159.90, 'https://example.com/p/saia-plissada.jpg', 'https://modaestilo.example.com/p/7', true, NOW(), NOW()),
    ('a08ebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'd3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'Calça Jogger Moletom Grey', 'Calça jogger confortável em algodão.', 'Inferiores', 139.90, 'https://example.com/p/jogger-grey.jpg', 'https://urbanwear.example.com/p/8', true, NOW(), NOW()),
    ('a09ebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'c3eebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'Trench Coat Clássico', 'Casaco impermeável clássico para inverno.', 'Casacos', 599.00, 'https://example.com/p/trench-coat.jpg', 'https://elegance.example.com/p/9', true, NOW(), NOW()),
    ('a11ebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c4eebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'Moletom Hoodie Graphic', 'Hoodie estampado com estampa urbana.', 'Superiores', 219.90, 'https://example.com/p/hoodie-graphic.jpg', 'https://streetculture.example.com/p/10', true, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- -------------------------------------------------------------
-- 3. WISHLIST, WARDROBE_ITEM & LOOK
-- -------------------------------------------------------------

INSERT INTO wishlist (id, customer_id, name, created_at, updated_at)
VALUES
    ('44eebc99-9c0b-4ef8-bb6d-6bb9bd380bbb', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Favoritos de Outono', NOW(), NOW()),
    ('55eebc99-9c0b-4ef8-bb6d-6bb9bd380ccc', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Streetwear Wishlist', NOW(), NOW()),
    ('b03ebc99-9c0b-4ef8-bb6d-6bb9bd380b03', 'a3eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'Looks de Festas', NOW(), NOW()),
    ('b04ebc99-9c0b-4ef8-bb6d-6bb9bd380b04', 'a4eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'Inverno 2026', NOW(), NOW()),
    ('b05ebc99-9c0b-4ef8-bb6d-6bb9bd380b05', 'a5eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'Viagem Europa', NOW(), NOW()),
    ('b06ebc99-9c0b-4ef8-bb6d-6bb9bd380b06', 'a6eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'Academia & Esporte', NOW(), NOW()),
    ('b07ebc99-9c0b-4ef8-bb6d-6bb9bd380b07', 'a7eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'Roupas de Trabalho', NOW(), NOW()),
    ('b08ebc99-9c0b-4ef8-bb6d-6bb9bd380b08', 'a8eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'Estilo Minimalista', NOW(), NOW()),
    ('b09ebc99-9c0b-4ef8-bb6d-6bb9bd380b09', 'a9eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'Looks de Primavera', NOW(), NOW()),
    ('b11ebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'a10ebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 'Básicos Essenciais', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

INSERT INTO wardrobe_item (id, customer_id, product_id, category, color, photo_url, created_at, updated_at)
VALUES
    ('66eebc99-9c0b-4ef8-bb6d-6bb9bd380ddd', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '11eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'Casacos', 'Bege', 'https://example.com/w/blazer.jpg', NOW(), NOW()),
    ('77eebc99-9c0b-4ef8-bb6d-6bb9bd380eee', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NULL, 'Camisetas', 'Branco', 'https://example.com/w/tshirt.jpg', NOW(), NOW()),
    ('88eebc99-9c0b-4ef8-bb6d-6bb9bd380fff', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '33eebc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'Casacos', 'Preto', 'https://example.com/w/jacket.jpg', NOW(), NOW()),
    ('d04ebc99-9c0b-4ef8-bb6d-6bb9bd380d04', 'a3eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'a04ebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'Vestidos', 'Vermelho', 'https://example.com/w/dress.jpg', NOW(), NOW()),
    ('d05ebc99-9c0b-4ef8-bb6d-6bb9bd380d05', 'a4eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'a05ebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'Camisas', 'Verde', 'https://example.com/w/shirt.jpg', NOW(), NOW()),
    ('d06ebc99-9c0b-4ef8-bb6d-6bb9bd380d06', 'a5eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'a06ebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'Camisetas', 'Branco', 'https://example.com/w/basic.jpg', NOW(), NOW()),
    ('d07ebc99-9c0b-4ef8-bb6d-6bb9bd380d07', 'a6eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'a08ebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'Calças', 'Cinza', 'https://example.com/w/jogger.jpg', NOW(), NOW()),
    ('d08ebc99-9c0b-4ef8-bb6d-6bb9bd380d08', 'a7eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'a07ebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'Saias', 'Preto', 'https://example.com/w/skirt.jpg', NOW(), NOW()),
    ('d09ebc99-9c0b-4ef8-bb6d-6bb9bd380d09', 'a8eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'a11ebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Moletom', 'Preto', 'https://example.com/w/hoodie.jpg', NOW(), NOW()),
    ('d10ebc99-9c0b-4ef8-bb6d-6bb9bd380d10', 'a9eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'a09ebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'Casacos', 'Bege', 'https://example.com/w/coat.jpg', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

INSERT INTO look (id, customer_id, title, is_ai_generated, occasion, photo_url, created_at, updated_at)
VALUES
    ('99eebc99-9c0b-4ef8-bb6d-6bb9bd380111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Look Trabalho Elegante', true, 'Trabalho', 'https://example.com/l/1.jpg', NOW(), NOW()),
    ('aa0ebc99-9c0b-4ef8-bb6d-6bb9bd380222', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Outing Casual Fim de Semana', false, 'Lazer', 'https://example.com/l/2.jpg', NOW(), NOW()),
    ('c03ebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'a3eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'Jantar Romântico', true, 'Jantar', 'https://example.com/l/3.jpg', NOW(), NOW()),
    ('c04ebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'a4eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'Streetwear Casual', false, 'Passeio', 'https://example.com/l/4.jpg', NOW(), NOW()),
    ('c05ebc99-9c0b-4ef8-bb6d-6bb9bd380c05', 'a5eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'Aerolook Confortável', true, 'Viagem', 'https://example.com/l/5.jpg', NOW(), NOW()),
    ('c06ebc99-9c0b-4ef8-bb6d-6bb9bd380c06', 'a6eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'Treino Esportivo', false, 'Academia', 'https://example.com/l/6.jpg', NOW(), NOW()),
    ('c07ebc99-9c0b-4ef8-bb6d-6bb9bd380c07', 'a7eebc99-9c0b-4ef8-bb6d-6bb9bd380a07', 'Reunião Executiva', true, 'Reunião', 'https://example.com/l/7.jpg', NOW(), NOW()),
    ('c08ebc99-9c0b-4ef8-bb6d-6bb9bd380c08', 'a8eebc99-9c0b-4ef8-bb6d-6bb9bd380a08', 'Show / Evento Noturno', false, 'Festa', 'https://example.com/l/8.jpg', NOW(), NOW()),
    ('c09ebc99-9c0b-4ef8-bb6d-6bb9bd380c09', 'a9eebc99-9c0b-4ef8-bb6d-6bb9bd380a09', 'Almoço em Família', true, 'Lazer', 'https://example.com/l/9.jpg', NOW(), NOW()),
    ('c10ebc99-9c0b-4ef8-bb6d-6bb9bd380c10', 'a10ebc99-9c0b-4ef8-bb6d-6bb9bd380a10', 'Minimalist Everyday', false, 'Diário', 'https://example.com/l/10.jpg', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- -------------------------------------------------------------
-- 4. TABELAS DE JUNÇÃO
-- -------------------------------------------------------------

INSERT INTO wishlist_item (id, wishlist_id, product_id, created_at)
VALUES
    ('bb1ebc99-9c0b-4ef8-bb6d-6bb9bd380333', '44eebc99-9c0b-4ef8-bb6d-6bb9bd380bbb', '11eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', NOW()),
    ('cc2ebc99-9c0b-4ef8-bb6d-6bb9bd380444', '44eebc99-9c0b-4ef8-bb6d-6bb9bd380bbb', '22eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', NOW()),
    ('dd3ebc99-9c0b-4ef8-bb6d-6bb9bd380555', '55eebc99-9c0b-4ef8-bb6d-6bb9bd380ccc', '33eebc99-9c0b-4ef8-bb6d-6bb9bd380a99', NOW()),
    ('e04ebc99-9c0b-4ef8-bb6d-6bb9bd380e04', 'b03ebc99-9c0b-4ef8-bb6d-6bb9bd380b03', 'a04ebc99-9c0b-4ef8-bb6d-6bb9bd380a04', NOW()),
    ('e05ebc99-9c0b-4ef8-bb6d-6bb9bd380e05', 'b04ebc99-9c0b-4ef8-bb6d-6bb9bd380b04', 'a09ebc99-9c0b-4ef8-bb6d-6bb9bd380a09', NOW()),
    ('e06ebc99-9c0b-4ef8-bb6d-6bb9bd380e06', 'b05ebc99-9c0b-4ef8-bb6d-6bb9bd380b05', 'a05ebc99-9c0b-4ef8-bb6d-6bb9bd380a05', NOW()),
    ('e07ebc99-9c0b-4ef8-bb6d-6bb9bd380e07', 'b06ebc99-9c0b-4ef8-bb6d-6bb9bd380b06', 'a08ebc99-9c0b-4ef8-bb6d-6bb9bd380a08', NOW()),
    ('e08ebc99-9c0b-4ef8-bb6d-6bb9bd380e08', 'b07ebc99-9c0b-4ef8-bb6d-6bb9bd380b07', 'a07ebc99-9c0b-4ef8-bb6d-6bb9bd380a07', NOW()),
    ('e09ebc99-9c0b-4ef8-bb6d-6bb9bd380e09', 'b08ebc99-9c0b-4ef8-bb6d-6bb9bd380b08', 'a06ebc99-9c0b-4ef8-bb6d-6bb9bd380a06', NOW()),
    ('e10ebc99-9c0b-4ef8-bb6d-6bb9bd380e10', 'b09ebc99-9c0b-4ef8-bb6d-6bb9bd380b09', 'a11ebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW())
    ON CONFLICT DO NOTHING;

INSERT INTO look_wardrobe_item (id, look_id, wardrobe_item_id, created_at)
VALUES
    ('ee4ebc99-9c0b-4ef8-bb6d-6bb9bd380666', '99eebc99-9c0b-4ef8-bb6d-6bb9bd380111', '66eebc99-9c0b-4ef8-bb6d-6bb9bd380ddd', NOW()),
    ('ff5ebc99-9c0b-4ef8-bb6d-6bb9bd380777', '99eebc99-9c0b-4ef8-bb6d-6bb9bd380111', '77eebc99-9c0b-4ef8-bb6d-6bb9bd380eee', NOW()),
    ('006ebc99-9c0b-4ef8-bb6d-6bb9bd380888', 'aa0ebc99-9c0b-4ef8-bb6d-6bb9bd380222', '88eebc99-9c0b-4ef8-bb6d-6bb9bd380fff', NOW()),
    ('f04ebc99-9c0b-4ef8-bb6d-6bb9bd380f04', 'c03ebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'd04ebc99-9c0b-4ef8-bb6d-6bb9bd380d04', NOW()),
    ('f05ebc99-9c0b-4ef8-bb6d-6bb9bd380f05', 'c04ebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'd05ebc99-9c0b-4ef8-bb6d-6bb9bd380d05', NOW()),
    ('f06ebc99-9c0b-4ef8-bb6d-6bb9bd380f06', 'c05ebc99-9c0b-4ef8-bb6d-6bb9bd380c05', 'd06ebc99-9c0b-4ef8-bb6d-6bb9bd380d06', NOW()),
    ('f07ebc99-9c0b-4ef8-bb6d-6bb9bd380f07', 'c06ebc99-9c0b-4ef8-bb6d-6bb9bd380c06', 'd07ebc99-9c0b-4ef8-bb6d-6bb9bd380d07', NOW()),
    ('f08ebc99-9c0b-4ef8-bb6d-6bb9bd380f08', 'c07ebc99-9c0b-4ef8-bb6d-6bb9bd380c07', 'd08ebc99-9c0b-4ef8-bb6d-6bb9bd380d08', NOW()),
    ('f09ebc99-9c0b-4ef8-bb6d-6bb9bd380f09', 'c08ebc99-9c0b-4ef8-bb6d-6bb9bd380c08', 'd09ebc99-9c0b-4ef8-bb6d-6bb9bd380d09', NOW()),
    ('f10ebc99-9c0b-4ef8-bb6d-6bb9bd380f10', 'c09ebc99-9c0b-4ef8-bb6d-6bb9bd380c09', 'd10ebc99-9c0b-4ef8-bb6d-6bb9bd380d10', NOW())
    ON CONFLICT DO NOTHING;

INSERT INTO look_product (id, look_id, product_id, created_at)
VALUES
    ('117ebc99-9c0b-4ef8-bb6d-6bb9bd380999', '99eebc99-9c0b-4ef8-bb6d-6bb9bd380111', '22eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', NOW()),
    ('228ebc99-9c0b-4ef8-bb6d-6bb9bd380aaa', 'aa0ebc99-9c0b-4ef8-bb6d-6bb9bd380222', '33eebc99-9c0b-4ef8-bb6d-6bb9bd380a99', NOW()),
    ('103ebc99-9c0b-4ef8-bb6d-6bb9bd380103', 'c03ebc99-9c0b-4ef8-bb6d-6bb9bd380c03', 'a04ebc99-9c0b-4ef8-bb6d-6bb9bd380a04', NOW()),
    ('104ebc99-9c0b-4ef8-bb6d-6bb9bd380104', 'c04ebc99-9c0b-4ef8-bb6d-6bb9bd380c04', 'a05ebc99-9c0b-4ef8-bb6d-6bb9bd380a05', NOW()),
    ('105ebc99-9c0b-4ef8-bb6d-6bb9bd380105', 'c05ebc99-9c0b-4ef8-bb6d-6bb9bd380c05', 'a06ebc99-9c0b-4ef8-bb6d-6bb9bd380a06', NOW()),
    ('106ebc99-9c0b-4ef8-bb6d-6bb9bd380106', 'c06ebc99-9c0b-4ef8-bb6d-6bb9bd380c06', 'a08ebc99-9c0b-4ef8-bb6d-6bb9bd380a08', NOW()),
    ('107ebc99-9c0b-4ef8-bb6d-6bb9bd380107', 'c07ebc99-9c0b-4ef8-bb6d-6bb9bd380c07', 'a07ebc99-9c0b-4ef8-bb6d-6bb9bd380a07', NOW()),
    ('108ebc99-9c0b-4ef8-bb6d-6bb9bd380108', 'c08ebc99-9c0b-4ef8-bb6d-6bb9bd380c08', 'a11ebc99-9c0b-4ef8-bb6d-6bb9bd380a11', NOW()),
    ('109ebc99-9c0b-4ef8-bb6d-6bb9bd380109', 'c09ebc99-9c0b-4ef8-bb6d-6bb9bd380c09', 'a09ebc99-9c0b-4ef8-bb6d-6bb9bd380a09', NOW()),
    ('110ebc99-9c0b-4ef8-bb6d-6bb9bd380110', 'c10ebc99-9c0b-4ef8-bb6d-6bb9bd380c10', '11eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', NOW())
    ON CONFLICT DO NOTHING;
