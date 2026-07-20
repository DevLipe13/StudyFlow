---
description: "Task list for StudyFlow auth, Keycloak dashboard and user management"
---

# Tasks: Autenticação Keycloak e Dashboard Inicial

**Input**: Design documents from `/specs/001-auth-keycloak-dashboard/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Testes unitários OBRIGATÓRIOS ao concluir cada funcionalidade (pedido
explícito + constituição StudyFlow). Cada incremento de lógica de negócio/
domínio ou regra de UI não trivial termina com task(s) de teste unitário
antes de considerar a história pronta.

**Organization**: Tarefas pequenas e entregáveis completos por user story —
sem deixar endpoints sem serviço, UI sem contrato ou regras sem testes.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependência incompleta)
- **[Story]**: [US1]…[US5] nas fases de história
- Sempre incluir caminho de arquivo

## Path Conventions

- Backend: `backend/src/main/java/com/studyflow/`, `backend/src/test/java/com/studyflow/`, `backend/src/main/resources/db/migration/`
- Frontend: `frontend/src/app/`, `frontend/src/environments/`, `frontend/src/app/**/*.spec.ts`
- Keycloak: `keycloak/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Estrutura vazia porém compilável dos três módulos

- [x] T001 Create root directories `backend/`, `frontend/`, `keycloak/` and root `docker-compose.yml` per plan.md
- [x] T002 [P] Initialize Quarkus Java 21 Maven project in `backend/pom.xml` with Hibernate ORM, Flyway, REST, OIDC, keycloak-admin-client dependencies
- [x] T003 [P] Initialize Angular 19+ TypeScript app in `frontend/` with signals-ready project layout under `frontend/src/app/`
- [x] T004 [P] Create Keycloak theme scaffold in `keycloak/themes/studyflow/` and `keycloak/docker-compose.yml` mounting the theme
- [x] T005 [P] Add Angular environment files `frontend/src/environments/environment.ts`, `environment.homologacao.ts`, `environment.treinamento.ts`, `environment.production.ts` with placeholders for keycloakUrl, realm, clientId, apiBaseUrl
- [x] T006 [P] Add backend config placeholders in `backend/src/main/resources/application.properties` using env vars only (no real secrets)
- [x] T007 [P] Add `.env.example` at repository root documenting required env vars (placeholder values only)
- [x] T008 [P] Configure backend unit test plugin (JUnit 5) in `backend/pom.xml` and frontend test runner in `frontend/package.json`

**Checkpoint**: `backend`, `frontend` e `keycloak` existem e builds básicos não falham por estrutura

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infra compartilhada que bloqueia todas as user stories

**⚠️ CRITICAL**: Nenhuma user story inicia antes desta fase

- [x] T009 Create Flyway migration `backend/src/main/resources/db/migration/V1__create_app_user.sql` for `app_user` per data-model.md
- [x] T010 [P] Create enums `ProfileType` and `EducationLevel` in `backend/src/main/java/com/studyflow/domain/`
- [x] T011 Create entity `User` in `backend/src/main/java/com/studyflow/domain/User.java` with camelCase fields mapping to snake_case columns
- [x] T012 Create `UserRepository` in `backend/src/main/java/com/studyflow/repository/UserRepository.java`
- [x] T013 [P] Implement CPF/email validation helpers in `backend/src/main/java/com/studyflow/domain/validation/CpfValidator.java` and `EmailValidator.java`
- [x] T014 Write unit tests for validators in `backend/src/test/java/com/studyflow/domain/validation/CpfValidatorTest.java` and `EmailValidatorTest.java`
- [x] T015 Configure Quarkus OIDC JWT validation in `backend/src/main/resources/application.properties` and security package under `backend/src/main/java/com/studyflow/security/`
- [x] T016 Implement `DisabledUserFilter` (or equivalent) rejecting soft-deleted users in `backend/src/main/java/com/studyflow/security/DisabledUserFilter.java`
- [x] T017 Write unit tests for disabled-user gate in `backend/src/test/java/com/studyflow/security/DisabledUserFilterTest.java`
- [x] T018 Implement `KeycloakAdminFacade` interface + Quarkus client wrapper in `backend/src/main/java/com/studyflow/service/KeycloakAdminFacade.java` (create/disable/enable/update credentials)
- [x] T019 Write unit tests for `KeycloakAdminFacade` with mocked admin client in `backend/src/test/java/com/studyflow/service/KeycloakAdminFacadeTest.java`
- [x] T020 [P] Add public health endpoint in `backend/src/main/java/com/studyflow/api/HealthResource.java` matching contracts
- [x] T021 [P] Secure all `/api/v1/**` business routes (authenticated) via Quarkus security config under `backend/src/main/java/com/studyflow/security/`
- [x] T022 Initialize keycloak-angular in `frontend/src/app/core/auth/keycloak-init.ts` and wire APP_INITIALIZER in `frontend/src/app/app.config.ts`
- [x] T023 Implement Bearer interceptor attaching access token in `frontend/src/app/core/auth/auth.interceptor.ts`
- [x] T024 Implement `authGuard` redirecting unauthenticated users in `frontend/src/app/core/auth/auth.guard.ts`
- [x] T025 Write unit tests for auth guard/interceptor helpers in `frontend/src/app/core/auth/auth.guard.spec.ts` and `auth.interceptor.spec.ts`
- [x] T026 [P] Create empty authenticated shell layout (mobile-first) in `frontend/src/app/layout/shell.component.ts`
- [x] T027 Document docker networking Keycloak↔Postgres↔apps in root `docker-compose.yml` and `keycloak/docker-compose.yml`

**Checkpoint**: JWT obrigatório nas rotas de negócio; User persistível; FE inicia Keycloak; validators e security cobertos por testes unitários

---

## Phase 3: User Story 1 — Criar conta de estudante (Priority: P1) 🎯 MVP

**Goal**: Auto-cadastro via tema Keycloak (perfil sempre estudante) + bootstrap do perfil local

**Independent Test**: Registrar estudante no tema, logar no Angular, confirmar `app_user` com `keycloakId`, CPF, perfil estudante e datas

### Implementation + unit tests

- [x] T028 [P] [US1] Customize Keycloak register/login theme forms in `keycloak/themes/studyflow/` including profile field fixed to estudante and fields name, cpf, birthDate, educationLevel
- [x] T029 [US1] Implement `UserBootstrapService` in `backend/src/main/java/com/studyflow/service/UserBootstrapService.java` creating local user from JWT `sub` + payload; enforce unique email/cpf among active users
- [x] T030 [US1] Write unit tests for bootstrap rules (happy path, duplicate email/cpf, always estudante) in `backend/src/test/java/com/studyflow/service/UserBootstrapServiceTest.java`
- [x] T031 [US1] Expose `POST /api/v1/users/me/bootstrap` and `GET /api/v1/users/me` in `backend/src/main/java/com/studyflow/api/UserResource.java` per `contracts/openapi.yaml`
- [x] T032 [US1] Write unit/resource tests for bootstrap/me mapping in `backend/src/test/java/com/studyflow/api/UserResourceBootstrapTest.java`
- [x] T033 [US1] Implement frontend auth post-login bootstrap call in `frontend/src/app/core/auth/user-bootstrap.service.ts`
- [x] T034 [US1] Write unit tests for bootstrap service (skip if profile exists, error mapping) in `frontend/src/app/core/auth/user-bootstrap.service.spec.ts`
- [x] T035 [US1] Ensure registration→login→bootstrap path lands on authenticated area without exposing secrets in `frontend/src/app/core/auth/` and theme messages under `keycloak/themes/studyflow/`

**Checkpoint**: US1 completa e testada — estudante novo tem identidade Keycloak + registro local válido

---

## Phase 4: User Story 2 — Entrar e ver dashboard inicial (Priority: P1)

**Goal**: Login OIDC no front (token/refresh/logout) + dashboard vazio; bloqueio de usuário desabilitado

**Independent Test**: Login estudante/admin ativos vê dashboard vazio; credenciais inválidas falham; soft-deleted/desabilitado não entra

### Implementation + unit tests

- [x] T036 [US2] Implement login redirect, silent/token refresh and logout (manual + session end) using keycloak-angular in `frontend/src/app/core/auth/auth.service.ts`
- [x] T037 [US2] Write unit tests for auth.service refresh/logout branches using falsy-friendly conditionals in `frontend/src/app/core/auth/auth.service.spec.ts`
- [x] T038 [P] [US2] Create empty dashboard feature page in `frontend/src/app/features/dashboard/dashboard.component.ts` and route in `frontend/src/app/app.routes.ts`
- [x] T039 [US2] Write unit tests for dashboard component render (empty state) in `frontend/src/app/features/dashboard/dashboard.component.spec.ts`
- [x] T040 [US2] Enforce backend rejection for disabled users on authenticated `/users/me` in `backend/src/main/java/com/studyflow/service/UserQueryService.java` (or filter integration)
- [x] T041 [US2] Write unit tests for disabled login/access denial in `backend/src/test/java/com/studyflow/service/UserQueryServiceTest.java`
- [x] T042 [US2] Map 401/403 from API to re-login UX without leaking secrets in `frontend/src/app/core/auth/auth-error.handler.ts` + unit tests in `auth-error.handler.spec.ts`

**Checkpoint**: US2 completa — sessão OIDC no FE, dashboard vazio, desabilitados bloqueados, testes unitários verdes

---

## Phase 5: User Story 3 — Menu conforme perfil (Priority: P2)

**Goal**: Menu Estudante (Dashboard, Criar tarefas, Sair) vs Admin (Dashboard, Usuários, Sair); placeholder de tarefas

**Independent Test**: Menus distintos por perfil; Criar tarefas só placeholder; Admin sem item de tarefas; logout limpa sessão

### Implementation + unit tests

- [x] T043 [US3] Implement profile-aware menu with signals in `frontend/src/app/layout/menu.component.ts` (estudante vs admin items)
- [x] T044 [US3] Write unit tests for menu visibility rules in `frontend/src/app/layout/menu.component.spec.ts`
- [x] T045 [P] [US3] Create tasks placeholder page in `frontend/src/app/features/tasks-placeholder/tasks-placeholder.component.ts` and route guarded for estudante only
- [x] T046 [US3] Write unit tests for estudante-only guard in `frontend/src/app/core/auth/student.guard.spec.ts` and placeholder component in `tasks-placeholder.component.spec.ts`
- [x] T047 [US3] Wire Admin users nav entry (route stub ok until US4) in `frontend/src/app/layout/menu.component.ts` and `frontend/src/app/app.routes.ts`
- [x] T048 [US3] Wire logout action in menu calling `auth.service` logout in `frontend/src/app/layout/menu.component.ts` + unit test coverage in `menu.component.spec.ts`
- [x] T049 [US3] Verify mobile-first layout styles for shell/menu in `frontend/src/app/layout/shell.component.scss` (or equivalent)

**Checkpoint**: US3 completa — menus corretos, placeholder, logout, testes de regras de visibilidade

---

## Phase 6: User Story 4 — Admin gerencia usuários (Priority: P2)

**Goal**: Admin consulta/edita perfil (exceto e-mail/senha), cria usuários (Keycloak+base, senha final), desabilita/reabilita sincronizado

**Independent Test**: Fluxos Admin do quickstart V4; não-Admin recebe 403; auto-desabilitar bloqueado

### Implementation + unit tests

- [x] T050 [US4] Implement `UserAdminService` create (Keycloak + local, final password) in `backend/src/main/java/com/studyflow/service/UserAdminService.java`
- [x] T051 [US4] Write unit tests for admin create (profile selection, uniqueness, no force password change) in `backend/src/test/java/com/studyflow/service/UserAdminServiceCreateTest.java`
- [x] T052 [US4] Implement admin update (name, cpf, birthDate, educationLevel, profile — never email/password) in `backend/src/main/java/com/studyflow/service/UserAdminService.java`
- [x] T053 [US4] Write unit tests for update constraints in `backend/src/test/java/com/studyflow/service/UserAdminServiceUpdateTest.java`
- [x] T054 [US4] Implement disable/enable syncing Keycloak + soft delete (`deletedAt`) with compensation/error on partial failure in `backend/src/main/java/com/studyflow/service/UserAdminService.java`
- [x] T055 [US4] Write unit tests for disable/enable success, self-disable forbidden, sync failure in `backend/src/test/java/com/studyflow/service/UserAdminServiceDisableEnableTest.java`
- [x] T056 [US4] Expose Admin endpoints `GET/POST /users`, `GET/PATCH /users/{id}`, `POST .../disable`, `POST .../enable` in `backend/src/main/java/com/studyflow/api/UserAdminResource.java` with `@RolesAllowed("admin")`
- [x] T057 [US4] Write unit/resource tests for Admin authorization and DTOs in `backend/src/test/java/com/studyflow/api/UserAdminResourceTest.java`
- [x] T058 [P] [US4] Build Admin users list/detail/create/edit UI using signals in `frontend/src/app/features/users/`
- [x] T059 [US4] Write unit tests for users feature services/components (role gating, form validation) under `frontend/src/app/features/users/*.spec.ts`
- [x] T060 [US4] Implement Admin disable/enable actions in UI calling API in `frontend/src/app/features/users/user-detail.component.ts` + unit tests in `user-detail.component.spec.ts`
- [x] T061 [US4] Add `adminGuard` in `frontend/src/app/core/auth/admin.guard.ts` + unit tests in `admin.guard.spec.ts`

**Checkpoint**: US4 completa e testada — gestão Admin end-to-end com sync Keycloak/base

---

## Phase 7: User Story 5 — Alterar e-mail/senha com aprovação e CPF (Priority: P2)

**Goal**: Fluxo solicitação → aprovação Admin → conclusão com CPF; senha nunca no Postgres

**Independent Test**: Quickstart V5; CPF inválido ou sem aprovação bloqueia; após conclusão login com novos dados

### Implementation + unit tests

- [x] T062 [US5] Create Flyway migration `backend/src/main/resources/db/migration/V2__create_login_change_request.sql` per data-model.md
- [x] T063 [P] [US5] Create enums/entity `LoginChangeRequest` in `backend/src/main/java/com/studyflow/domain/`
- [x] T064 [US5] Implement `LoginChangeService` state machine (pending→approved/rejected→completed) in `backend/src/main/java/com/studyflow/service/LoginChangeService.java`
- [x] T065 [US5] Write unit tests for all transitions and CPF check in `backend/src/test/java/com/studyflow/service/LoginChangeServiceTest.java`
- [x] T066 [US5] Apply email/password changes via `KeycloakAdminFacade` on complete (never persist password) in `LoginChangeService.java`
- [x] T067 [US5] Write unit tests ensuring password not stored and Keycloak update called in `backend/src/test/java/com/studyflow/service/LoginChangeServiceKeycloakTest.java`
- [x] T068 [US5] Expose login-change endpoints from `contracts/openapi.yaml` in `backend/src/main/java/com/studyflow/api/LoginChangeResource.java`
- [x] T069 [US5] Write unit/resource tests in `backend/src/test/java/com/studyflow/api/LoginChangeResourceTest.java`
- [x] T070 [P] [US5] Build estudante request + complete UI in `frontend/src/app/features/login-change/`
- [x] T071 [P] [US5] Build Admin approve/reject UI in `frontend/src/app/features/login-change/admin-login-change.component.ts`
- [x] T072 [US5] Write unit tests for login-change FE validators/state handling under `frontend/src/app/features/login-change/*.spec.ts`

**Checkpoint**: US5 completa e testada — fluxo FR-021 fechado com cobertura unitária

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Fechamento de qualidade, docs e consistência

- [x] T073 [P] Align OpenAPI docs comments with implemented routes in `specs/001-auth-keycloak-dashboard/contracts/openapi.yaml` if paths drifted
- [x] T074 [P] Update root `README.md` in pt-BR with how to run docker, backend, frontend (no secrets)
- [x] T075 [P] Add seed/bootstrap notes for first Admin in `specs/001-auth-keycloak-dashboard/quickstart.md` cross-links
- [x] T076 Run full unit test suites: `backend` `./mvnw test` and `frontend` `npm test`; fix failures
- [x] T077 Gap-fill any missing unit tests for new domain logic under `backend/src/test/java/` and `frontend/src/app/**/*.spec.ts`
- [x] T078 Security pass: confirm no secrets in repo examples; CPF masking in list DTOs in `backend/src/main/java/com/studyflow/api/dto/`
- [x] T079 Mobile viewport smoke check against quickstart V6 for shell/menu/dashboard
- [x] T080 Conventional Commits hygiene reminder: group commits per completed story (`feat:`, `test:`, `docs:`) via PR to `main`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)** → sem dependências
- **Phase 2 (Foundational)** → depende da Phase 1; **bloqueia** todas as stories
- **US1 (Phase 3)** → após Phase 2 — **MVP**
- **US2 (Phase 4)** → após Phase 2; idealmente após US1 (bootstrap melhora o fluxo, mas login dashboard pode validar com usuário seed)
- **US3 (Phase 5)** → após US2 (precisa shell autenticado + dashboard)
- **US4 (Phase 6)** → após Phase 2 + menu Admin (US3); recomenda-se pós US2
- **US5 (Phase 7)** → após US1/US2 (estudante autenticado) e US4 (Admin aprova)
- **Phase 8 (Polish)** → após stories desejadas

### User Story Dependencies

| Story | Depende de | Independente para testar? |
|-------|------------|---------------------------|
| US1 | Phase 2 | Sim — cadastro + bootstrap |
| US2 | Phase 2 (+ seed ou US1) | Sim — login + dashboard |
| US3 | US2 | Sim — menus/placeholder/logout |
| US4 | Phase 2, US3 (nav) | Sim — API Admin com token admin |
| US5 | US2 + US4 | Sim — fluxo login-change completo |

### Within Each Story

1. Implementar unidade de comportamento completa (serviço/UI/endpoint)
2. **Imediatamente** escrever testes unitários daquela unidade
3. Só então seguir para a próxima unidade
4. Models/migrations antes de services; services antes de resources; API antes de UI que consome

### Parallel Opportunities

- Setup: T002–T008 em paralelo
- Foundational: T010/T013/T020/T022 em paralelo após T009 onde aplicável
- US4 UI (T058) paralelo a testes de service já verdes
- US5 FE estudante/admin (T070/T071) em paralelo após API estável

---

## Parallel Example: User Story 1

```bash
# Após T029 service pronto:
Task: "Unit tests UserBootstrapServiceTest.java"
# Em paralelo com tema (se pessoas distintas):
Task: "Customize Keycloak theme in keycloak/themes/studyflow/"
Task: "user-bootstrap.service.ts + .spec.ts no frontend"
```

---

## Parallel Example: User Story 4

```bash
Task: "UserAdminServiceCreateTest.java"
Task: "UserAdminServiceDisableEnableTest.java"
# Após API:
Task: "frontend users feature + *.spec.ts"
Task: "admin.guard.ts + admin.guard.spec.ts"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Phase 1 + Phase 2  
2. Phase 3 (US1) com testes unitários de cada unidade  
3. **STOP** e validar Independent Test da US1 + quickstart V1  

### Incremental Delivery

1. US1 → cadastro/bootstrap  
2. US2 → login + dashboard  
3. US3 → menus + placeholder  
4. US4 → gestão Admin  
5. US5 → alteração e-mail/senha  
6. Polish  

Cada story só é “done” com: comportamento completo **e** testes unitários da lógica nova verdes.

---

## Notes

- Preferir condicionais falsy no Angular (`if (!user)`, `profile || 'estudante'`) conforme research.md
- Senha nunca em Postgres; só Keycloak Admin API
- Commits Conventional Commits; integração só via Pull Request
- Docs em pt-BR; código/identificadores em inglês; atributos camelCase

---

## Phase 9: Convergence

**Purpose**: Fechar gaps entre spec/plan/tasks e o código atual (pós `/speckit-implement`)

- [X] T081 CRITICAL: Implement Keycloak `register.ftl` (and login overlay if needed) with name, CPF, birthDate, educationLevel, and profile fixed to Estudante under `keycloak/themes/studyflow/login/` per FR-017, US1/AC1 (missing)
- [X] T082 CRITICAL: Show profile as Estudante on registration and always submit `profile=estudante` (remove hide-only CSS approach) in `keycloak/themes/studyflow/login/` per FR-017 (contradicts)
- [X] T083 CRITICAL: Extend `keycloak/realm/studyflow-realm.json` with custom user attributes and protocol mappers for cpf, birthDate, educationLevel, profile so bootstrap can consume them per FR-004, FR-017 (missing)
- [X] T084 CRITICAL: Add global disabled-user gate (filter or harden `CurrentUserService.requireUser`) rejecting soft-deleted users on all authenticated API routes in `backend/src/main/java/com/studyflow/security/` per FR-008, US2/AC3 (missing)
- [X] T085 CRITICAL: Assign realm roles via Keycloak Admin role-mapping API after user create in `KeycloakAdminFacadeImpl.java` (do not rely on `setRealmRoles` alone) per FR-020 (partial)
- [X] T086 CRITICAL: Sync admin profile updates (name/attributes/profile roles) to Keycloak in `UserAdminService.update` per FR-019 (partial)
- [X] T087 Enforce `LoginChangeService.complete` to require `newEmail` match approved `proposedEmail` (and active uniqueness) before Keycloak update per FR-021 (partial)
- [X] T088 Add compensation for partial failures on disable/enable and emailAndPassword login-change so Keycloak and DB stay consistent in `UserAdminService` / `LoginChangeService` per FR-021, FR-023, FR-024 (partial)
- [X] T089 Add menu entries for login-change (estudante request/complete, admin approvals) in `frontend/src/app/layout/menu/` linked to existing routes per FR-021, US5 (missing)
- [X] T090 CRITICAL unit tests gap: add `DisabledUserFilterTest`/`UserQueryServiceTest`, `UserAdminResourceTest`, `LoginChangeServiceKeycloakTest`, and mocked `KeycloakAdminFacadeImpl` coverage under `backend/src/test/java/` per Constitution V (missing)
- [X] T091 Fix account theme mismatch: add minimal `keycloak/themes/studyflow/account/` or set realm `accountTheme` to a stock theme in `studyflow-realm.json` per plan: Keycloak theme (partial)
- [X] T092 Surface user-facing API errors and Admin form select labels in Portuguese brasileiro per FR-015 (partial)
- [X] T093 Remove usable secret defaults from `backend/src/main/resources/application.properties` (keep placeholders only in `.env.example`) per Constitution IV, FR-012 (partial)
- [X] T094 Improve student login-change UX to select approved request from list (no manual UUID) in `frontend/src/app/features/login-change/` per US5 (partial)
- [X] T095 Rewrite `auth.guard.spec.ts` to exercise the real exported `authGuard` (login redirect, disabled user, success) and add missing users/auth-error specs per T025, Constitution V (partial)
