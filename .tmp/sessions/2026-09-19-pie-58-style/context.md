# Task Context: PIE-58 US09 Backend questionário de estilo (Plano A)

Session ID: 2026-09-19-pie-58-style
Created: 2026-09-19T00:00:00Z
Status: in_progress
Approval: usuário aprovou Plano A em 2026-09-19 ("pode executar o plano A")

## Current Request
Executar o Plano A aprovado: backend do questionário de estilo (PIE-58).

Requisitos Linear (US09):
- Enum Style + StyleQuestion/StyleOption/StyleAnswer
- GET /style/questions
- POST /users/me/style/answers com validações (todas respondidas, option pertence à question)
- Cálculo por maioria simples, persistência como STRING
- Reexecução sobrescreve respostas anteriores

## Context Files (Standards to Follow)
Repo sem pasta de standards (.opencode/context inexistente, paths.json inexistente).
Standards = padrões Maven/Java existentes do próprio repo (AGENTS.md + docs/agent/):
- docs/agent/workflow.md (RETRIEVE → UNDERSTAND → INSPECT → TEACH → PLAN → CONFIRM → IMPLEMENT)
- docs/agent/planning.md, validation.md
- Convenções observadas: entidades JPA com UUID (@UuidGenerator), AuditableEntity, records para DTOs com Bean Validation, Services @Transactional, Repositories Spring Data, AuthenticatedUserProvider (X-User-Id provisório), GlobalExceptionHandler (BusinessException→409, IllegalArgumentException→400, ResourceNotFound→404), migrations Flyway V1__create_schema.sql / V2__seed.sql (DO $$ + NOW())

## Reference Files (Source Material to Look At)
- src/main/java/com/ages/pie/domain/enums/UserRole.java (exemplo de enum)
- src/main/java/com/ages/pie/domain/entity/User.java (tabela customer, sem style_result — adicionar)
- src/main/java/com/ages/pie/domain/entity/BodyProfile.java (style_preference varchar[] texto livre — conflito documentado, não alterar)
- src/main/java/com/ages/pie/domain/entity/Product.java (padrão entidade com validação no construtor)
- src/main/java/com/ages/pie/domain/entity/AuditableEntity.java
- src/main/java/com/ages/pie/application/controller/UserController.java
- src/main/java/com/ages/pie/application/controller/WishlistController.java (padrão com AuthenticatedUserProvider)
- src/main/java/com/ages/pie/application/service/UserService.java
- src/main/java/com/ages/pie/application/service/WishlistService.java (padrão service + userNotFound)
- src/main/java/com/ages/pie/application/dto/user/UserResponseDTO.java
- src/main/java/com/ages/pie/application/dto/wishlist/AddWishlistItemRequestDTO.java
- src/main/java/com/ages/pie/infrastructure/security/AuthenticatedUserProvider.java
- src/main/java/com/ages/pie/infrastructure/repository/UserRepository.java
- src/main/java/com/ages/pie/application/exception/GlobalExceptionHandler.java, BusinessException.java
- src/main/resources/db/migration/V1__create_schema.sql, V2__seed.sql
- pom.xml (Java 21, Spring Boot 4.0.7)

## External Docs Fetched
Nenhum (sem dependências externas novas).

## Components
1. Enum Style (STRING persistido)
2. Entidades StyleQuestion / StyleOption / StyleAnswer + repositories
3. Migration V6 schema + V7 seed (perguntas iniciais)
4. DTOs (question/option/answers request/result) + StyleScoreCalculator (maioria simples, desempate: primeira com maior contagem em ordem de pergunta)
5. StyleService (GET questions ordenadas + ativas; POST valida tudo respondido + option pertence à question + usuário existe; apaga respostas anteriores e salva novas; calcula e persiste customer.style_result)
6. StyleController (GET /style/questions, POST /users/me/style/answers via AuthenticatedUserProvider)

## Constraints
- Persistir estilo como STRING (varchar), nunca ordinal
- StyleAnswer PK (customer_id, question_id) — uma resposta por pergunta por usuário; reexecução = delete + insert (sobrescrita)
- FK option→question validada também em service (não só FK)
- Não alterar BodyProfile.style_preference (escopo separado)
- Questionnaire seed inicial provisório (conteúdo oficial/imagens em aberto no planejamento)
- Sem Lombok nas entidades (seguir padrão manual existente)

## Exit Criteria
- [ ] `mvn compile` passa
- [ ] GET /style/questions retorna perguntas + opções ativas ordenadas
- [ ] POST /users/me/style/answers valida e persiste, maioria simples define style_result
- [ ] Re-POST sobrescreve (sem duplicadas)
- [ ] style_result persistido como STRING em customer
