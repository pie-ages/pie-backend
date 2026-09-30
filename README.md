
# Piê — Backend

API REST da plataforma Piê Consultoria de Imagem.

## Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 21 | Linguagem e runtime |
| Spring Boot | API e configuração da aplicação |
| Spring Data JPA | Persistência de dados |
| PostgreSQL | Banco de dados |
| Docker Compose | Ambiente local da aplicação e do banco |
| OpenAPI / Swagger | Documentação da API |

## Pré-requisitos

- Java 21
- Docker e Docker Compose, para subir o ambiente completo

## Como executar

Na raiz do repositório, inicie a API e o PostgreSQL:

```bash
docker compose up --build
```

Após a inicialização, a API fica disponível em `http://localhost:8080` e a documentação Swagger em `http://localhost:8080/swagger-ui/index.html`.

## Testes

```bash
./mvnw test
```

## Estilos do usuário

`GET/PUT /users/me/style` usa os estilos `Style` do questionário e as preferências da US09. `GET/PUT /users/me/product-styles` usa `ProductStyle` na coluna `customer.styles`. As duas rotas exigem autenticação e persistem dados separados; os valores de um enum não são convertidos automaticamente para o outro.

## Object Storage

O projeto usa **Supabase Storage** para armazenar imagens. Há dois buckets:

| Bucket | Uso |
| --- | --- |
| `product-images` | Imagens dos produtos do catálogo |
| `wardrobe-items` | Imagens das peças do guarda-roupa (PIE-74) |

### Configuração local

Copie `src/main/resources/application-local.properties.example` para `src/main/resources/application-local.properties` e preencha:

```properties
supabase.storage.url=https://<ref>.supabase.co
supabase.storage.service-role-key=eyJ...
supabase.storage.bucket=product-images
supabase.storage.wardrobe-bucket=wardrobe-items
```

A `service-role-key` fica em: **Supabase Dashboard → Project Settings → API → service_role → Reveal**.

### Criar o bucket `wardrobe-items`

1. Acesse o Supabase Dashboard → **Storage → New bucket**
2. Nome: `wardrobe-items`
3. Marque **Public bucket** para que as URLs públicas funcionem
4. Salve

### Variáveis necessárias por ambiente

| Propriedade | Env var equivalente | Obrigatória | Descrição |
| --- | --- | --- | --- |
| `supabase.storage.url` | `SUPABASE_STORAGE_URL` | Sim | URL base do projeto Supabase |
| `supabase.storage.service-role-key` | `SUPABASE_STORAGE_SERVICE_ROLE_KEY` | Sim | Chave de acesso com permissão de escrita |
| `supabase.storage.bucket` | `SUPABASE_STORAGE_BUCKET` | Não (padrão: `product-images`) | Bucket para imagens de produtos |
| `supabase.storage.wardrobe-bucket` | `SUPABASE_STORAGE_WARDROBE_BUCKET` | Não (padrão: `wardrobe-items`) | Bucket para imagens do guarda-roupa |
| `supabase.storage.max-file-size-mb` | `SUPABASE_STORAGE_MAX_FILE_SIZE_MB` | Não (padrão: `5`) | Limite de tamanho de upload em MB |

Em ambientes Docker e CI/CD, use as env vars. Localmente via IDE ou `./mvnw`, use `application-local.properties`.

> **Segurança:** O arquivo `application-local.properties` está no `.gitignore` e nunca deve ser commitado.

## Identificação de estilo

`POST /users/me/style/identify` consolida o estilo do usuário autenticado a partir dos dados já cadastrados e grava o resultado em `body_profile.identified_style`. A regra é determinística e não usa IA.

| Fonte | Origem | Peso |
| --- | --- | --- |
| Respostas de estilo | `body_profile.style_preference` | 2 por ocorrência |
| Cores favoritas | `body_profile.favorite_colors` | 1 por ocorrência |

- Valores de `style_preference` que não correspondem a um id de `ProductStyle` são ignorados; nenhum estilo novo é criado.
- Cada cor favorita (hexadecimal) é associada ao estilo cuja cor de referência está mais próxima em distância RGB.
- Vence o estilo de maior pontuação. Em caso de empate, vale a ordem de declaração do enum `ProductStyle`.
- Sem nenhum dado aproveitável, a resposta é `{"styles": []}` e nada é gravado, preservando um estilo definido manualmente.

A identificação só ocorre nesta chamada explícita, e nenhum outro fluxo escreve em `identified_style`.

## Convenções de branch e commit

### Fluxo de branches

`main` contém versões estáveis e `dev` recebe a integração das mudanças aprovadas. Não faça commits diretamente nessas branches: crie uma branch a partir de `dev` e abra um pull request para `dev`.

Use o formato abaixo, incluindo o identificador da issue do Linear quando ele existir:

```text
<tipo>/<linear-id>-<descricao-curta>
```

Exemplos:

```text
feature/PIE-123-criar-cadastro-de-usuario
bugfix/PIE-456-corrigir-validacao-de-email
docs/atualizar-readme
```

| Tipo | Quando usar |
| --- | --- |
| `feature` | Nova funcionalidade. |
| `bugfix` | Correção de defeito. |
| `refactor` | Melhoria interna sem alterar o comportamento esperado. |
| `docs` | Criação ou atualização de documentação. |
| `chore` | Manutenção, dependências ou configuração. |
| `deploy` | Preparação ou ajuste de publicação. |
| `infra` | Infraestrutura, CI/CD ou serviços de suporte. |

### Commits

Use Conventional Commits:

```text
<tipo>: <descricao no imperativo>
```

Exemplos:

```text
feat: adiciona cadastro
fix: corrige expiração do token
refactor: centraliza tratamento de erros
```

- Escreva a descrição em letras minúsculas, no imperativo e sem ponto final;
- Faça commits pequenos e independentes; não misture feature, correção e refatoração.

## Pull requests

Use o template do repositório, informe a issue no Linear e inclua evidências quando aplicável. Antes de abrir o PR, atualize a sua branch com `dev`.
