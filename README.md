
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
<tipo>(<escopo>): <descricao no imperativo> [LINEAR-ID]
```

Exemplos:

```text
feat/adiciona cadastro
fix/corrige expiração do token
refactor/centraliza tratamento de erros
```

- Escreva a descrição em letras minúsculas, no imperativo e sem ponto final;
- Faça commits pequenos e independentes; não misture feature, correção e refatoração.

## Pull requests

Use o template do repositório, informe a issue no Linear e inclua evidências quando aplicável. Antes de abrir o PR, atualize a sua branch com `dev`.
