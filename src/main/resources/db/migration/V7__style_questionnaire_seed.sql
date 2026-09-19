-- -------------------------------------------------------------
-- PIE-58 (US09): questionário de estilo — seed inicial
--
-- Conteúdo provisório: 5 perguntas x 4 opções, cada opção mapeada
-- para um Style. Ajustar textos/imagens quando o conteúdo oficial
-- do questionário for definido.
-- -------------------------------------------------------------

DO $$
DECLARE
    v_q1 uuid; v_q2 uuid; v_q3 uuid; v_q4 uuid; v_q5 uuid;
BEGIN
    INSERT INTO style_question (question_text, display_order, active, created_at, updated_at)
    VALUES ('Qual look você usaria no dia a dia?', 1, true, NOW(), NOW())
    RETURNING id INTO v_q1;

    INSERT INTO style_question (question_text, display_order, active, created_at, updated_at)
    VALUES ('Qual peça é indispensável no seu guarda-roupa?', 2, true, NOW(), NOW())
    RETURNING id INTO v_q2;

    INSERT INTO style_question (question_text, display_order, active, created_at, updated_at)
    VALUES ('Como você se veste para sair à noite?', 3, true, NOW(), NOW())
    RETURNING id INTO v_q3;

    INSERT INTO style_question (question_text, display_order, active, created_at, updated_at)
    VALUES ('Quais cores predominam nas suas roupas?', 4, true, NOW(), NOW())
    RETURNING id INTO v_q4;

    INSERT INTO style_question (question_text, display_order, active, created_at, updated_at)
    VALUES ('Qual acessório tem mais a sua cara?', 5, true, NOW(), NOW())
    RETURNING id INTO v_q5;

    INSERT INTO style_option (question_id, label, style, display_order, created_at, updated_at)
    VALUES
        (v_q1, 'Jeans e camiseta básica', 'CASUAL', 1, NOW(), NOW()),
        (v_q1, 'Alfaiataria bem cortada', 'ELEGANTE', 2, NOW(), NOW()),
        (v_q1, 'Moletom e tênis esportivo', 'ESPORTIVO', 3, NOW(), NOW()),
        (v_q1, 'Peças fluidas e estampadas', 'BOHO', 4, NOW(), NOW());

    INSERT INTO style_option (question_id, label, style, display_order, created_at, updated_at)
    VALUES
        (v_q2, 'Jaqueta jeans confortável', 'CASUAL', 1, NOW(), NOW()),
        (v_q2, 'Blazer estruturado', 'ELEGANTE', 2, NOW(), NOW()),
        (v_q2, 'Vestido delicado', 'ROMANTICO', 3, NOW(), NOW()),
        (v_q2, 'Camisa branca minimalista', 'MINIMALISTA', 4, NOW(), NOW());

    INSERT INTO style_option (question_id, label, style, display_order, created_at, updated_at)
    VALUES
        (v_q3, 'Look monocromático simples', 'MINIMALISTA', 1, NOW(), NOW()),
        (v_q3, 'Vestido com leveza e movimento', 'ROMANTICO', 2, NOW(), NOW()),
        (v_q3, 'Terno ou vestido sofisticado', 'ELEGANTE', 3, NOW(), NOW()),
        (v_q3, 'Mix de texturas e acessórios', 'BOHO', 4, NOW(), NOW());

    INSERT INTO style_option (question_id, label, style, display_order, created_at, updated_at)
    VALUES
        (v_q4, 'Tons neutros: preto, branco, cinza', 'MINIMALISTA', 1, NOW(), NOW()),
        (v_q4, 'Cores básicas do dia a dia', 'CASUAL', 2, NOW(), NOW()),
        (v_q4, 'Cores vibrantes e enérgicas', 'ESPORTIVO', 3, NOW(), NOW()),
        (v_q4, 'Tons pastéis e delicados', 'ROMANTICO', 4, NOW(), NOW());

    INSERT INTO style_option (question_id, label, style, display_order, created_at, updated_at)
    VALUES
        (v_q5, 'Boné ou mochila esportiva', 'ESPORTIVO', 1, NOW(), NOW()),
        (v_q5, 'Joias delicadas', 'ROMANTICO', 2, NOW(), NOW()),
        (v_q5, 'Relógio clássico', 'ELEGANTE', 3, NOW(), NOW()),
        (v_q5, 'Colares e pulseiras artesanais', 'BOHO', 4, NOW(), NOW());
END $$;
