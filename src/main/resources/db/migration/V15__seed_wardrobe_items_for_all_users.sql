DO $$
DECLARE
    v_user uuid;
    emails text[] := ARRAY[
        'ana.silva@example.com',
        'carlos.eduardo@example.com',
        'mariana.oliveira@example.com',
        'lucas.mendes@example.com',
        'beatriz.costa@example.com',
        'gabriel.souza@example.com',
        'juliana.lima@example.com',
        'rodrigo.ferreira@example.com',
        'camila.rocha@example.com',
        'felipe.santos@example.com'
    ];
    e text;
BEGIN
    FOREACH e IN ARRAY emails LOOP
        SELECT id INTO v_user FROM customer WHERE email = e;
        IF v_user IS NULL THEN CONTINUE; END IF;

        INSERT INTO wardrobe_item (customer_id, name, category, color, photo_url, created_at, updated_at)
        VALUES
        (v_user, 'Camisa',  'Camisa',  'Azul',      'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=600&q=80', NOW(), NOW()),
        (v_user, 'Calça',   'Calça',   'Preta',     'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600&q=80', NOW(), NOW()),
        (v_user, 'Tênis',   'Tênis',   'Branco',    'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=600&q=80', NOW(), NOW()),
        (v_user, 'Casaco',  'Casaco',  'Terracota', 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600&q=80', NOW(), NOW());
    END LOOP;
END $$;
