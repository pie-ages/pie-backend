## Descrição da Mudança

- Descreva objetivamente o problema resolvido e a abordagem adotada.

## Issue no Linear

- Cole aqui o link da issue.

## Tipo de Mudança

- [ ] Bugfix - Correção de bug
- [ ] Feature - Adição de nova funcionalidade
- [ ] Refactoring - Refatoração de código
- [ ] Documentation - Atualização de documentação
- [ ] Chore - Tarefas de manutenção
- [ ] Deploy - Deploy de nova versão
- [ ] Infrastructure - Atualização de infraestrutura

## Validação de Arquitetura

- [ ] Controllers em `application/controller` apenas recebem requisições, delegam para services e retornam respostas HTTP.
- [ ] DTOs e mapeamentos foram mantidos em `application/dto` e `application/mapper`.
- [ ] Regras de negócio foram mantidas em `application/service` ou `domain/service`, sem depender de HTTP, JPA ou segurança.
- [ ] Entidades e enums foram mantidos em `domain/entity` e `domain/enums`.
- [ ] Acesso a dados, JWT e demais detalhes técnicos foram mantidos em `infrastructure`.
- [ ] Configurações de aplicação foram mantidas em `application/config`.

## Checklist

- [ ] `./mvnw test` foi executado com sucesso.
- [ ] Testes unitários com JUnit e Mockito foram adicionados ou atualizados para regras de negócio alteradas.
- [ ] Testes de integração foram adicionados ou atualizados para endpoints e persistência alterados.
- [ ] Segurança, autenticação e autorização foram revisadas quando aplicável.
- [ ] Configurações, variáveis de ambiente ou Docker foram atualizados quando necessário.
- [ ] A documentação foi atualizada quando necessário.
- [ ] Não há logs, comentários temporários ou código morto.

## Evidências

- Adicione prints, resposta de endpoint ou outra evidência de funcionamento quando aplicável.
