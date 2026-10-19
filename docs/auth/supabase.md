# Supabase Auth

O backend usa o Supabase Auth (e-mail e senha) e atua como:

- **Proxy** para login, renovação e logout (`/auth/login`, `/auth/refresh`, `/auth/logout`), chamando a API GoTrue do projeto.
- **Resource server**: todo endpoint fora da lista pública exige `Authorization: Bearer <access_token>`. O token é validado com:
  - assinatura assimétrica, via JWKS (`<SUPABASE_URL>/auth/v1/.well-known/jwks.json`);
  - `iss` (`<SUPABASE_URL>/auth/v1`), `aud` (`authenticated`) e `exp`/`nbf`.

O `sub` do token é o id do usuário no Supabase e também o `customer.id` no nosso banco. Por isso, o cadastro deve criar o customer com esse id. `GET /auth/me` devolve o customer do token, ou 404 se ele ainda não tiver cadastro.

## Endpoints

| Método | Rota | Auth | Resposta |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | pública | `{accessToken, refreshToken, expiresIn, tokenType, user}` |
| POST | `/api/auth/refresh` | pública | mesmo corpo do login (o refresh token é rotacionado) |
| POST | `/api/auth/logout` | Bearer | 204; revoga os refresh tokens da sessão |
| GET | `/api/auth/me` | Bearer | `UserSummaryDTO` |

Erros: credenciais ou refresh inválidos → 401; e-mail não confirmado → 403; Supabase fora do ar → 503; token ausente ou inválido → 401.

Logout: o access token continua válido até expirar (no máximo 1h), porque o JWT é stateless. A sessão não pode mais ser renovada.

## Variáveis por ambiente

| Env var | Onde obter |
| --- | --- |
| `SUPABASE_URL` | Project Settings → Data API → Project URL |
| `SUPABASE_PUBLISHABLE_KEY` | Project Settings → API Keys → Publishable key (ou a legacy `anon`) |

Nenhuma das duas tem default. Sem elas a aplicação não sobe. Nunca versione os valores: use `.env` ou `application-local.properties` (ambos no `.gitignore`) e os secrets do ambiente de deploy. O backend **não** precisa do JWT secret nem da `service_role` para autenticar.

## Configuração do projeto (dashboard) — dev e prod

Faça em cada projeto Supabase:

1. **Authentication → Sign In / Providers → Email**: habilitado.
   - **Confirm email**: **ligado em prod**. Em dev, desligue se o time preferir testar sem caixa de e-mail.
2. **Authentication → URL Configuration**:
   - **Site URL**: a URL do front do ambiente.
   - **Redirect URLs**: as URLs de confirmação de e-mail e de reset de senha do app (deep link do Expo e web).
3. **Project Settings → JWT Keys**:
   - Migre para **JWT Signing Keys** assimétricas (ECC P-256 / RS256).
   - O backend aceita ES256 e RS256 e **rejeita HS256**.
4. **Authentication → Sessions**:
   - Access token expiry: `3600`.
   - Refresh token rotation: habilitado.
   - Reuse interval: `10`s.

## Local

`supabase/config.toml` sobe o Auth local com `supabase start`, sem confirmação de e-mail e com as mesmas regras de sessão. Depois:

```bash
export SUPABASE_URL=http://127.0.0.1:54321
export SUPABASE_PUBLISHABLE_KEY=<publishable key exibida pelo `supabase status`>
```

Para usar o Auth local com JWKS, o stack precisa de chaves assimétricas. Gere com `supabase gen signing-key --algorithm ES256` e configure `signing_keys_path` em `[auth]`. Se preferir, aponte para o projeto Supabase de dev.
