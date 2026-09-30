DO $$
DECLARE
    v_renner    uuid;
    v_zara      uuid;
    v_cea       uuid;
    v_hm        uuid;
    v_riachuelo uuid;
BEGIN
    SELECT id INTO v_renner    FROM company WHERE cnpj = '92693249000195';
    SELECT id INTO v_zara      FROM company WHERE cnpj = '02332886000104';
    SELECT id INTO v_cea       FROM company WHERE cnpj = '45698435000126';
    SELECT id INTO v_hm        FROM company WHERE cnpj = '02773575000180';
    SELECT id INTO v_riachuelo FROM company WHERE cnpj = '61716327000195';

    UPDATE product SET material = ARRAY['poliester','la']       WHERE company_id = v_renner AND name = 'Blazer Social Feminino';
    UPDATE product SET material = ARRAY['denim']                WHERE company_id = v_renner AND name = 'Calça Jeans Wide Leg Feminina';
    UPDATE product SET material = ARRAY['poliester']            WHERE company_id = v_renner AND name = 'Saia Midi Plissada';
    UPDATE product SET material = ARRAY['linho']                WHERE company_id = v_renner AND name = 'Vestido Linho Midi';
    UPDATE product SET material = ARRAY['denim']                WHERE company_id = v_renner AND name = 'Jaqueta Jeans Feminina';
    UPDATE product SET material = ARRAY['la','poliester']       WHERE company_id = v_renner AND name = 'Calça Alfaiataria Slim';
    UPDATE product SET material = ARRAY['algodao','viscose']    WHERE company_id = v_renner AND name = 'Top Cropped Canelado';
    UPDATE product SET material = ARRAY['denim']                WHERE company_id = v_renner AND name = 'Short Jeans Destroyed';
    UPDATE product SET material = ARRAY['algodao']              WHERE company_id = v_renner AND name = 'Camisa Social Branca';
    UPDATE product SET material = ARRAY['algodao','poliester']  WHERE company_id = v_renner AND name = 'Trench Coat Bege';
    UPDATE product SET material = ARRAY['poliester','viscose']  WHERE company_id = v_renner AND name = 'Saia Lápis Midi';
    UPDATE product SET material = ARRAY['viscose']              WHERE company_id = v_renner AND name = 'Blusa Manga Bufante';
    UPDATE product SET material = ARRAY['algodao']              WHERE company_id = v_renner AND name = 'Moletom Oversize Premium';
    UPDATE product SET material = ARRAY['algodao']              WHERE company_id = v_renner AND name = 'Bermuda Cargo Masculina';
    UPDATE product SET material = ARRAY['poliester','viscose']  WHERE company_id = v_renner AND name = 'Vestido Midi Estampado';

    UPDATE product SET material = ARRAY['poliester']            WHERE company_id = v_renner AND name = 'Calça Legging Básica Preta';
    UPDATE product SET material = ARRAY['algodao']              WHERE company_id = v_renner AND name = 'Camiseta Oversized Feminina';
    UPDATE product SET material = ARRAY['viscose']              WHERE company_id = v_renner AND name = 'Vestido Curto Floral';
    UPDATE product SET material = ARRAY['tricot']               WHERE company_id = v_renner AND name = 'Blusa Tricô Cropped';
    UPDATE product SET material = ARRAY['poliester']            WHERE company_id = v_renner AND name = 'Jaqueta Bomber Feminina';
    UPDATE product SET material = ARRAY['tricot','la']          WHERE company_id = v_renner AND name = 'Cardigan Longo Feminino';
    UPDATE product SET material = ARRAY['algodao']              WHERE company_id = v_renner AND name = 'Regata Básica Algodão';
    UPDATE product SET material = ARRAY['denim']                WHERE company_id = v_renner AND name = 'Calça Skinny Jeans Feminina';

    UPDATE product SET material = ARRAY['la','poliester']       WHERE company_id = v_zara      AND name = 'Jaqueta Oversized Preta';
    UPDATE product SET material = ARRAY['algodao','poliester']  WHERE company_id = v_zara      AND name = 'Calça Jogger Moletom';

    UPDATE product SET material = ARRAY['viscose','poliester']  WHERE company_id = v_cea       AND name = 'Vestido Midi Floral';
    UPDATE product SET material = ARRAY['poliester','algodao']  WHERE company_id = v_cea       AND name = 'Trench Coat Clássico';

    UPDATE product SET material = ARRAY['algodao']              WHERE company_id = v_hm        AND name = 'Camisa Cargo Verde Militar';
    UPDATE product SET material = ARRAY['algodao','poliester']  WHERE company_id = v_hm        AND name = 'Moletom Hoodie Estampado';

    UPDATE product SET material = ARRAY['algodao']              WHERE company_id = v_riachuelo AND name = 'Camiseta Básica Algodão';
END $$;
