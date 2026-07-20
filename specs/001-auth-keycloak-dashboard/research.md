# Research: Autenticação Keycloak e Dashboard Inicial

**Feature**: `001-auth-keycloak-dashboard` | **Date**: 2026-07-15

## 1. Stack frontend

**Decision**: Angular 19+ com TypeScript, signals para estado reativo,
condicionais privilegiando valores falsy (`if (!user)`, `user?.role`, etc.)
para reduzir branches. Autenticação com `keycloak-angular` + `keycloak-js`.
Configs globais em `src/environments/` (`environment.ts`,
`environment.homologacao.ts`, `environment.treinamento.ts`,
`environment.production.ts`) com `url` do Keycloak, `realm`, `clientId`,
`apiBaseUrl` — sem secrets de Admin.

**Rationale**: Atende à exigência de signals/falsy e ao fluxo de
login/token/refresh/logout totalmente no frontend.

**Alternatives considered**:
- Auth code exchange no backend (BFF) — rejeitado: usuário pediu fluxo no front
- React — rejeitado: Angular definido

## 2. Stack backend

**Decision**: Quarkus (Java 21), Hibernate ORM, PostgreSQL, Flyway,
`quarkus-oidc` para validar Bearer JWT do Keycloak, e
`quarkus-keycloak-admin-client` para criar/atualizar/desabilitar usuários
no IdP. Todas as rotas de negócio exigem autenticação; health/liveliness
podem ficar públicas apenas se necessário ao Docker.

**Rationale**: Alinha ao pedido explícito e ao padrão Quarkus + Keycloak.

**Alternatives considered**:
- Spring Boot — rejeitado: Quarkus definido
- Armazenar senha no Postgres — rejeitado: violaria constituição/segurança

## 3. Flyway

**Decision**: Migrations em `backend/src/main/resources/db/migration/` com
nomenclatura oficial Flyway:

- Versionadas: `V<MAJOR>[.<MINOR>]__<description>.sql`
  Ex.: `V1__create_user_table.sql`, `V2__create_login_change_request.sql`
- Descrição em snake_case/lowercase inglês
- Sem `Undo` nesta fase (KISS)
- Repeatable só se necessário para views (`R__...`) — não previsto no MVP

**Rationale**: Convenção documentada no Flyway; histórico linear e
revisável em PR.

**Alternatives considered**: Liquibase — rejeitado: Flyway definido;
Hibernate `hbm2ddl` em produção — rejeitado: sem versionamento.

## 4. Tema Keycloak + Docker

**Decision**: Pasta `keycloak/` com tema `themes/studyflow/` (login e
registro). `docker-compose` monta o tema e sobe Keycloak. Registro no tema
expõe campo de perfil fixo/oculto como `estudante` (custom attribute /
role). Atributos custom: `cpf`, `birthDate`, `educationLevel`, `fullName`
(ou mapping equivalente). Sem client secrets versionados.

**Rationale**: FR-016/FR-017; isolamento do IdP via Docker.

**Alternatives considered**: Tema só na imagem pré-buildada sem pasta no
repo — rejeitado: spec exige pasta dedicada versionada.

## 5. Sincronização perfil local após auto-cadastro

**Decision**: Após o primeiro login OIDC bem-sucedido (ou redirect pós-
registro), o frontend chama `POST /api/v1/users/me/bootstrap` com os dados
de perfil necessários se o usuário ainda não existir na base. O backend
usa o `sub` do JWT como `keycloakId`, valida unicidade de e-mail/CPF e
grava perfil `estudante`. Campos podem vir do token/userinfo (mappers) ou
payload complementar validado.

**Rationale**: Evita SPI Keycloak custom (mais complexo); mantém FE
responsável pelo OIDC e BE pela persistência.

**Alternatives considered**:
- Event Listener SPI no Keycloak — mais ops/complexidade
- Só Admin cria usuários — rejeitado: auto-cadastro no tema é requisito

## 6. Autorização por perfil

**Decision**: Role/claim `estudante` | `admin` mapeada no Keycloak
(realm/client roles) e espelhada no campo `profile` da tabela `app_user`.
Backend valida JWT + role para rotas Admin; também rejeita usuários com
`deletedAt != null` / `disabled = true` mesmo com token válido.

**Rationale**: Dupla barreira (IdP + soft delete local) atende FR-008 e
desabilitar sincronizado.

**Alternatives considered**: Só roles no JWT sem espelho local — frágil
para soft delete e consultas Admin.

## 7. Desabilitar / reabilitar

**Decision**: Operação transacional de negócio: (1) Keycloak Admin API
`enabled=false|true`; (2) set/clear `deletedAt` (soft delete) e
`updatedAt` na base. Em falha do segundo passo após o primeiro, compensar
ou falhar a API com erro explícito (sem estado silencioso divergente).
Admin não pode desabilitar a própria conta (regra de proteção).

**Rationale**: Spec FR-023/024 + edge cases de consistência.

## 8. Alteração de e-mail/senha (FR-021)

**Decision**: Entidade `LoginChangeRequest` com estados `PENDING`,
`APPROVED`, `REJECTED`, `COMPLETED`. Estudante cria; Admin aprova/rejeita;
estudante completa com CPF + novos dados; backend aplica via Keycloak
Admin API (e-mail/senha) e atualiza e-mail local se aplicável. Senha nunca
é persistida no Postgres.

**Rationale**: Fluxo pedido na clarificação (Opção C).

## 9. Bootstrap do primeiro Admin

**Decision**: Script/documentação no quickstart: criar usuário Admin no
Keycloak (ou via compose realm import) + registrar perfil local com
`profile=admin` (seed Flyway opcional `V3__seed_admin_placeholder.sql`
somente com dados fictícios de dev, ou passo manual).

**Rationale**: Assumido na spec como decisão de plano.

## 10. Environments Angular

**Decision**: Um arquivo por ambiente alinhado às branches de deploy
(`homologacao`, `treinamento`, `production`/`main`). Build usa
`fileReplacements` / `application` configs do Angular. Nenhum secret de
service account no frontend.

**Rationale**: Pedido explícito; segregação de configs globais.

## Decisions summary (NEEDS CLARIFICATION)

Nenhum item permanece como NEEDS CLARIFICATION — stack e integrações foram
fixcidas pelo usuário e resolvidas acima.
