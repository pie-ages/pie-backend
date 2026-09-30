DELETE FROM product
WHERE name IN ('Macacão Jeans Feminino', 'Conjunto Alfaiataria Preto')
  AND company_id = (SELECT id FROM company WHERE cnpj = '92693249000195');
