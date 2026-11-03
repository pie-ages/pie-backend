# Supabase Auth

O backend usa o Supabase Auth (e-mail e senha) e atua como:

- **Proxy** para login, renovação, logout e recuperação de senha (`/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/recover`, `/auth/recover/verify`, `/auth/password`), chamando a API GoTrue do projeto.
- **Resource server**: todo endpoint fora da lista pública exige `Authorization: Bearer <access_token>`. O token é validado com:
  - assinatura assimétrica, via JWKS (`<SUPABASE_URL>/auth/v1/.well-known/jwks.json`);
  - `iss` (`<SUPABASE_URL>/auth/v1`), `aud` (`authenticated`) e `exp`/`nbf`.

O `sub` do token é o id da identidade no Supabase. O `auth.users` fica em outro banco que não o da aplicação, então a associação com o customer é a coluna `customer.supabase_user_id` (sem FK), preenchida no cadastro. Toda rota autenticada traduz o `sub` para o id local; um token válido sem cadastro correspondente responde 404, inclusive em `GET /auth/me`.

## Endpoints

| Método | Rota | Auth | Resposta |
| --- | --- | --- | --- |
| POST | `/api/users` | pública | cadastro de cliente |
| POST | `/api/companies` | pública | cadastro de empresa |
| POST | `/api/auth/login` | pública | `{accessToken, refreshToken, expiresIn, tokenType, user}` |
| POST | `/api/auth/refresh` | pública | mesmo corpo do login (o refresh token é rotacionado) |
| POST | `/api/auth/logout` | Bearer | 204; revoga os refresh tokens da sessão |
| POST | `/api/auth/recover` | pública | 204, mesmo para e-mail não cadastrado; envia o código de recuperação |
| POST | `/api/auth/recover/verify` | pública | mesmo corpo do login |
| PUT | `/api/auth/password` | Bearer da recuperação | 204; troca a senha |
| GET | `/api/auth/me` | Bearer | `UserSummaryDTO` |

Erros: credenciais ou refresh inválidos → 401; e-mail não confirmado → 403; Supabase fora do ar → 503; token ausente ou inválido → 401; token válido sem cadastro local → 404; código de recuperação inválido ou expirado → 400; senha igual à atual ou fraca → 400; troca de senha fora da sessão de recuperação → 403; excesso de pedidos → 429.

Logout: o access token continua válido até expirar (no máximo 1h), porque o JWT é stateless. A sessão não pode mais ser renovada.

## Recuperação de senha

A recuperação usa um código de 6 dígitos enviado por e-mail, sem link nem deep link:

1. `POST /api/auth/recover` com `{email}`: o Supabase envia o código só se a conta existir.
2. `POST /api/auth/recover/verify` com `{email, code}`: valida o código e devolve uma sessão.
3. `PUT /api/auth/password` com `{password}` e o `accessToken` do passo 2: troca a senha.

- O código vale por 1h e é de uso único. Um pedido novo invalida o anterior.
- Entre dois pedidos para o mesmo e-mail é preciso esperar 60s; antes disso, a resposta é 429. Esse 429 só ocorre para e-mail cadastrado, então revela que a conta existe, como o 409 do cadastro.
- Validar o código e trocar a senha são passos separados: senha igual ou fraca responde 400 sem consumir o código, e a mesma sessão pode tentar outra senha.
- A troca só aceita a sessão emitida pelo verify, identificada pelo claim `amr` com `method: "otp"`; o token de um login com senha recebe 403. Se o projeto passar a usar login por código ou magic link, essas sessões também serão aceitas.
- Trocar a senha encerra as outras sessões do usuário (o access token delas vale até expirar). A sessão da recuperação continua válida.
- Clientes e empresas usam as mesmas rotas.
- Os limites por IP do Supabase são compartilhados por todos os usuários, porque as chamadas saem do backend. A proteção contra tentativas de adivinhar o código depende desse limite.

O template **Authentication → Emails → Templates → Reset password** precisa mostrar o código com `{{ .Token }}`.

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
   - **Email OTP Length**: `6`. O backend só aceita códigos de 6 dígitos.
   - **Email OTP Expiration**: `3600`.
   - **Minimum password length**: `6`.
2. **Authentication → URL Configuration**:
   - **Site URL**: a URL do front do ambiente.
   - **Redirect URLs**: nenhuma é necessária. O cadastro confirma o e-mail pelo admin API e a recuperação de senha usa código.
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

O template de recuperação de senha não está versionado no `config.toml`. Localmente o e-mail sai no padrão do Supabase, só com link, e o fluxo por código não funciona; para testá-lo, aponte para o projeto de dev.
