ALTER TABLE customer
    ADD COLUMN favorite_colors varchar[] NOT NULL DEFAULT ARRAY[]::varchar[];

WITH reference_colors (color_name, reference_hex, priority) AS (
    VALUES
        ('BEGE', 'D2B48C', 1),
        ('AZUL', '0000FF', 2),
        ('PRETO', '000000', 3),
        ('BRANCO', 'FFFFFF', 4),
        ('CINZA', '808080', 5),
        ('VERDE', '008000', 6),
        ('VERMELHO', 'FF0000', 7),
        ('ROSA', 'FFC0CB', 8),
        ('TERRACOTA', 'E2725B', 9),
        ('OFF_WHITE', 'FAF0E6', 10),
        ('AMARELO', 'FFFF00', 11),
        ('LARANJA', 'FFA500', 12),
        ('ROXO', '800080', 13),
        ('LILAS', 'C8A2C8', 14),
        ('MARROM', '8B4513', 15),
        ('CARAMELO', 'C68E17', 16),
        ('CREME', 'FFFDD0', 17),
        ('NUDE', 'E3BC9A', 18),
        ('CORAL', 'FF7F50', 19),
        ('SALMAO', 'FA8072', 20),
        ('VINHO', '800020', 21),
        ('BORDO', '800000', 22),
        ('AZUL_MARINHO', '000080', 23),
        ('AZUL_CLARO', '87CEFA', 24),
        ('AZUL_PETROLEO', '004B5A', 25),
        ('VERDE_OLIVA', '808000', 26),
        ('VERDE_MUSGO', '556B2F', 27),
        ('VERDE_MENTA', '98FF98', 28),
        ('CINZA_CLARO', 'D3D3D3', 29),
        ('CINZA_ESCURO', '404040', 30)
),
reference_rgb AS (
    SELECT
        color_name,
        priority,
        get_byte(decode(reference_hex, 'hex'), 0) AS red,
        get_byte(decode(reference_hex, 'hex'), 1) AS green,
        get_byte(decode(reference_hex, 'hex'), 2) AS blue
    FROM reference_colors
),
original_colors AS (
    SELECT
        profile.customer_id,
        item.position,
        CASE
            WHEN item.hex ~ '^#[0-9A-Fa-f]{6}$'
            THEN decode(substring(item.hex FROM 2), 'hex')
            ELSE NULL
        END AS rgb
    FROM body_profile profile
    CROSS JOIN LATERAL unnest(profile.favorite_colors)
        WITH ORDINALITY AS item(hex, position)
),
mapped_colors AS (
    SELECT
        original.customer_id,
        original.position,
        nearest.color_name
    FROM original_colors original
    CROSS JOIN LATERAL (
        SELECT reference.color_name
        FROM reference_rgb reference
        ORDER BY
            (get_byte(original.rgb, 0) - reference.red)
                * (get_byte(original.rgb, 0) - reference.red)
            + (get_byte(original.rgb, 1) - reference.green)
                * (get_byte(original.rgb, 1) - reference.green)
            + (get_byte(original.rgb, 2) - reference.blue)
                * (get_byte(original.rgb, 2) - reference.blue),
            reference.priority
        LIMIT 1
    ) nearest
    WHERE original.rgb IS NOT NULL
),
unique_colors AS (
    SELECT
        customer_id,
        color_name,
        min(position) AS first_position
    FROM mapped_colors
    GROUP BY customer_id, color_name
),
aggregated_colors AS (
    SELECT
        customer_id,
        array_agg(color_name::varchar ORDER BY first_position) AS colors
    FROM unique_colors
    GROUP BY customer_id
)
UPDATE customer
SET favorite_colors = COALESCE(aggregated.colors, ARRAY[]::varchar[])
FROM body_profile profile
LEFT JOIN aggregated_colors aggregated
    ON aggregated.customer_id = profile.customer_id
WHERE customer.id = profile.customer_id;