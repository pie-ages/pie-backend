UPDATE product
SET color = v.color
FROM (VALUES
    ('Blazer Social Feminino',        'Bege'),
    ('Calça Jeans Wide Leg Feminina', 'Azul'),
    ('Jaqueta Oversized Preta',       'Preto'),
    ('Vestido Midi Floral',           'Vermelho'),
    ('Camisa Cargo Verde Militar',    'Verde'),
    ('Camiseta Básica Algodão',       'Branco'),
    ('Saia Midi Plissada',            'Preto'),
    ('Calça Jogger Moletom',          'Cinza'),
    ('Trench Coat Clássico',          'Bege'),
    ('Moletom Hoodie Estampado',      'Preto')
) AS v (name, color)
WHERE product.name = v.name;
