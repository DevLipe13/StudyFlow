# Implementation Plan: Autenticação Keycloak e Dashboard Inicial

**Branch**: `001-auth-keycloak-dashboard` | **Date**: 2026-07-15 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-auth-keycloak-dashboard/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Entregar a base do StudyFlow com frontend Angular e backend Quarkus
separados, identidade via Keycloak (tema Docker em `keycloak/`), perfil
local em PostgreSQL (Hibernate + Flyway), autenticação JWT validada no
backend e fluxo de login/token/refresh/logout no frontend via adapter
Keycloak. Após autenticação: dashboard vazio, menu por perfil (Estudante /
Admin), gestão administrativa de usuários (CRUD de perfil, desabilitar/
reabilitar sincronizado com Keycloak) e fluxo de alteração de e-mail/senha
com aprovação + CPF.

## Technical Context

**Language/Version**: Java 21 (Quarkus); TypeScript 5.x (Angular 19+)

**Primary Dependencies**:
- Backend: Quarkus, Hibernate ORM with Panache (or equivalent), Flyway,
  quarkus-oidc (validação JWT), quarkus-keycloak-admin-client,
  SmallRye JWT/OIDC
- Frontend: Angular (signals, falsy-first conditionals), keycloak-angular /
  keycloak-js, environment files por ambiente
- Infra: Docker Compose — PostgreSQL, Keycloak com tema montado de `keycloak/`

**Storage**: PostgreSQL (perfil de usuário, solicitações de alteração de login)

**Testing**: JUnit 5 + Quarkus Test (backend unit/integration);
  Jasmine/Jest + Angular TestBed (frontend unit); contratos REST validados
  manualmente via quickstart

**Target Platform**: Web responsivo (mobile-first); API HTTP; Keycloak
  containerizado

**Project Type**: Web application (frontend + backend + idP Keycloak)

**Performance Goals**: Login e exibição do dashboard em < 1 min (SC-002);
  cadastro em < 3 min (SC-001); API autenticada com resposta interativa
  típica de app pessoal (sem SLA de alta escala nesta fase)

**Constraints**:
- Credenciais/segredos só via variáveis de ambiente
- Rotas de backend exigem autenticação (Bearer JWT Keycloak), exceto health
  se necessário para orquestração
- Login/token/refresh/logout 100% no frontend (adapter Keycloak)
- Atributos/código em inglês (camelCase); docs em pt-BR
- Migrations Flyway com nomenclatura oficial (`V<VERSION>__<description>.sql`)
- Tema Keycloak versionado em `keycloak/` e servido via Docker

**Scale/Scope**: Uso pessoal/estudantil; MVP de auth + dashboard + gestão
  Admin; “Criar tarefas” apenas placeholder; sem CRUD de tarefas

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Verificar conformidade com `.specify/memory/constitution.md` (StudyFlow v1.0.0+):

| Gate | Status pré-Phase 0 | Status pós-Phase 1 |
|------|--------------------|--------------------|
| **Mobile-First** | PASS — UI Angular responsiva; shell/menu prioriza viewport móvel | PASS — quickstart valida mobile; contratos UI sem desktop-only |
| **Clean Code e SOLID** | PASS — camadas resource/service/repository; FE por features | PASS — data-model e API separam responsabilidades |
| **KISS** | PASS — OIDC padrão + Admin API Keycloak; sem BFF extra | PASS — ver Complexity Tracking (apenas stacks exigidos) |
| **Segurança** | PASS — env vars; senhas só no Keycloak; JWT no FE sem persistir
  em código; Admin client secret via env | PASS — contratos sem secrets; soft delete sincronizado |
| **Testes unitários** | PASS — lógica de domínio (user, login-change, disable sync)
  com testes unitários obrigatórios | PASS — escopo de testes listado em research/quickstart |
| **Linguagem** | PASS — docs pt-BR; código EN; camelCase nos atributos Java/TS | PASS |
| **Fluxo** | PASS — Conventional Commits; PR; branches `main` /
  `homologacao` / `treinamento` | PASS |

**Gate result**: PASS (sem violações injustificadas).

## Project Structure

### Documentation (this feature)

```text
specs/001-auth-keycloak-dashboard/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md             # gerado por /speckit-tasks
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/studyflow/
│   ├── domain/           # entities, enums
│   ├── repository/
│   ├── service/          # UserService, LoginChangeService, KeycloakAdminFacade
│   ├── api/              # JAX-RS resources
│   ├── security/         # role checks, disabled-user filter
│   └── config/
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/     # Flyway: V1__....sql
└── src/test/java/

frontend/
├── src/app/
│   ├── core/             # auth interceptor, guards, keycloak init
│   ├── shared/
│   ├── features/
│   │   ├── dashboard/
│   │   ├── users/        # admin gestão
│   │   ├── login-change/
│   │   └── tasks-placeholder/
│   └── layout/           # shell + menu por perfil (signals)
├── src/environments/     # environment.ts, environment.homologacao.ts, ...
└── public/

keycloak/
├── themes/studyflow/     # tema custom (login/register, perfil=estudante)
├── realm/                # export/import opcional do realm (sem secrets reais)
└── docker-compose.yml    # Keycloak (+ link/volume do tema); pode referenciar postgres idP

docker-compose.yml        # orquestra postgres app, keycloak, opcional backend
```

**Structure Decision**: Aplicação web com `frontend/`, `backend/` e
`keycloak/` na raiz do repositório (FR-001, FR-016). Tema Keycloak sob
Docker montando `keycloak/themes`. Configs de ambiente Angular em
`src/environments/`; segredos Quarkus/Keycloak/Postgres via env.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Três pastas raiz (frontend, backend, keycloak) | Spec exige FE/BE separados + pasta de tema Keycloak | Monólito não atende FR-001/FR-016 |
| keycloak-admin-client no Quarkus | Admin cria/desabilita usuários no IdP | Só UI Keycloak não cobre gestão na plataforma nem sync desabilitar |
| Fluxo login-change com estados | Spec exige solicitação → aprovação → CPF | Admin alterar senha direto viola FR-021 |
