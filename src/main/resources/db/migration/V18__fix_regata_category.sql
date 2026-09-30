UPDATE product
SET category = 'camiseta'
WHERE name = 'Regata Básica Algodão'
  AND company_id = (SELECT id FROM company WHERE cnpj = '92693249000195');
