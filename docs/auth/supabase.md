# Supabase Auth

O backend usa o Supabase Auth (e-mail e senha) e atua como:

- **Proxy** para login, renovação e logout (`/auth/login`, `/auth/refresh`, `/auth/logout`), chamando a API GoTrue do projeto.
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
| GET | `/api/auth/me` | Bearer | `UserSummaryDTO` |

Erros: credenciais ou refresh inválidos → 401; e-mail não confirmado → 403; Supabase fora do ar → 503; token ausente ou inválido → 401; token válido sem cadastro local → 404.

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

## E-mails (SMTP)

Os e-mails do Auth, como o de recuperação de senha, são enviados pelo próprio Supabase, pelo SMTP configurado em cada projeto. O backend não envia e-mail nem recebe credenciais de SMTP.

| Ambiente | SMTP |
| --- | --- |
| Local | Inbucket do `supabase start`; os e-mails aparecem em `http://localhost:54324` |
| Dev | Mailtrap Email Sandbox |

### Provedor

- **Mailtrap Email Sandbox** (dev): captura os e-mails numa caixa web, sem entregar ao destinatário, e não exige domínio. Plano gratuito: 50 e-mails/mês e 1 usuário.
- **Resend**: avaliado e não adotado por enquanto. O plano gratuito tem 3.000 e-mails/mês (100/dia), mas sem domínio verificado só entrega para o e-mail do dono da conta.

A configuração de dev foi feita no Mailtrap, na conta pessoal do Natan.

### Configurar um projeto

1. No Mailtrap, abra **Email Sandbox → My Sandbox → Integration → SMTP** e copie usuário e senha.
2. No Supabase, em **Authentication → Emails → SMTP Settings**, ative **Enable custom SMTP** e preencha:

   | Campo | Valor |
   | --- | --- |
   | Sender email address | `naoresponda@pie.test` |
   | Sender name | `PIE` |
   | Host | `sandbox.smtp.mailtrap.io` |
   | Port number | `587` |
   | Username / Password | credenciais do passo 1 |

   Use a porta 587 (ou 2525). O Mailtrap usa STARTTLS em todas as portas, e na 465 o Supabase espera TLS direto.
3. Valide: em **Authentication → Users**, abra o menu de um usuário e use **Send password recovery**. O e-mail deve aparecer no sandbox do Mailtrap. Se não aparecer, veja **Logs → Auth**.

O sandbox não valida remetente nem domínio. Um provedor com entrega real exige domínio verificado (SPF e DKIM) antes do primeiro envio.

### Credenciais

Usuário e senha do SMTP ficam só no painel do Supabase, que os armazena criptografados. Não há variáveis de SMTP no backend, e os valores nunca vão para o repositório. Se a senha vazar, gere outra em **Reset Credentials** no Mailtrap e atualize o painel.

## Local

`supabase/config.toml` sobe o Auth local com `supabase start`, sem confirmação de e-mail e com as mesmas regras de sessão. Depois:

```bash
export SUPABASE_URL=http://127.0.0.1:54321
export SUPABASE_PUBLISHABLE_KEY=<publishable key exibida pelo `supabase status`>
```

Para usar o Auth local com JWKS, o stack precisa de chaves assimétricas. Gere com `supabase gen signing-key --algorithm ES256` e configure `signing_keys_path` em `[auth]`. Se preferir, aponte para o projeto Supabase de dev.
