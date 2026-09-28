UPDATE style_question
SET active = false
WHERE display_order > 4;

UPDATE style_question
SET question_text = CASE display_order
    WHEN 1 THEN 'Qual look você usaria?'
    WHEN 2 THEN 'E num evento mais informal?'
    WHEN 3 THEN 'Qual desses você calçaria hoje?'
    WHEN 4 THEN 'E pra uma festa?'
END
WHERE display_order BETWEEN 1 AND 4;

UPDATE style_option AS option
SET label = CASE question.display_order
        WHEN 1 THEN CASE option.display_order WHEN 1 THEN 'Alfaiataria Chic' ELSE 'Casual Alinhado' END
        WHEN 2 THEN CASE option.display_order WHEN 1 THEN 'Vestido Florido' ELSE 'Vestido All Black' END
        WHEN 3 THEN CASE option.display_order WHEN 1 THEN 'Tênis' ELSE 'Sapatilha' END
        WHEN 4 THEN CASE option.display_order WHEN 1 THEN 'Jaqueta em couro' ELSE 'Jaqueta Terracota' END
    END,
    style = CASE question.display_order
        WHEN 1 THEN CASE option.display_order WHEN 1 THEN 'ELEGANTE' ELSE 'CASUAL' END
        WHEN 2 THEN CASE option.display_order WHEN 1 THEN 'ROMANTICO' ELSE 'ELEGANTE' END
        WHEN 3 THEN CASE option.display_order WHEN 1 THEN 'ESPORTIVO' ELSE 'ELEGANTE' END
        WHEN 4 THEN CASE option.display_order WHEN 1 THEN 'CASUAL' ELSE 'BOHO' END
    END,
    image_url = 'https://fjpdxidknltrczqqwbas.supabase.co/storage/v1/object/public/style-quiz/q' || question.display_order || '-' ||
        CASE option.display_order WHEN 1 THEN 'a' ELSE 'b' END || '.png'
FROM style_question AS question
WHERE option.question_id = question.id
    AND question.display_order BETWEEN 1 AND 4
    AND option.display_order IN (1, 2);
    