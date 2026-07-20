# Quickstart: Autenticação Keycloak e Dashboard Inicial

**Feature**: `001-auth-keycloak-dashboard` | **Date**: 2026-07-15

Guia de validação ponta a ponta após a implementação. Detalhes de modelo e
API: [data-model.md](./data-model.md), [contracts/openapi.yaml](./contracts/openapi.yaml).

## Pré-requisitos

- Docker e Docker Compose
- JDK 21+
- Node.js LTS (compatível com Angular 19+)
- Variáveis de ambiente (exemplos com placeholders — sem valores reais):

```bash
export POSTGRES_USER=studyflow
export POSTGRES_PASSWORD=<password>
export POSTGRES_DB=studyflow
export KC_HOSTNAME=localhost
export KC_ADMIN=<admin>
export KC_ADMIN_PASSWORD=<password>
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/studyflow
export QUARKUS_DATASOURCE_USERNAME=studyflow
export QUARKUS_DATASOURCE_PASSWORD=<password>
export QUARKUS_OIDC_AUTH_SERVER_URL=http://localhost:8080/realms/studyflow
export QUARKUS_OIDC_CLIENT_ID=studyflow-api
export KC_ADMIN_CLIENT_ID=studyflow-admin-cli
export KC_ADMIN_CLIENT_SECRET=<secret>
```

Frontend: preencher `frontend/src/environments/environment.ts` com
`keycloakUrl`, `realm`, `clientId` (público) e `apiBaseUrl`.

## Subir dependências

```bash
# Na raiz do repositório — Postgres app + Keycloak com tema de keycloak/
docker compose up -d
# ou: docker compose -f keycloak/docker-compose.yml up -d  (conforme layout final)
```

Verificar: Keycloak acessível, tema `studyflow` aplicado no realm, client
público Angular e client confidencial (só backend) configurados via env.

## Backend

```bash
cd backend
./mvnw quarkus:dev
# Flyway aplica V1__, V2__, ...
```

Esperado: app sobe; `GET /api/v1/health` (se exposto) responde 200;
rotas `/users/**` retornam 401 sem Bearer.

## Frontend

```bash
cd frontend
npm install
npm start
```

Esperado: app redireciona para login Keycloak (adapter); após login, shell
com menu e dashboard vazio.

## Bootstrap do primeiro Admin

1. Criar usuário Admin no Keycloak (senha final) e role/claim `admin`.
2. Fazer login no Angular com esse usuário.
3. Garantir perfil local `admin` (bootstrap/seed documentado na implementação).
4. Validar menu: Dashboard + gestão de usuários; sem “Criar tarefas”.

## Cenários de validação

### V1 — Auto-cadastro Estudante (tema Keycloak)

1. Abrir registro no tema Keycloak (perfil Estudante).
2. Preencher nome, e-mail, CPF, nascimento, formação, senha.
3. Concluir login no Angular → bootstrap local.
4. Ver dashboard vazio + menu (Dashboard, Criar tarefas, Sair).

**OK**: registro em `app_user` com `keycloakId`, `profile=estudante`,
`deletedAt` nulo.

### V2 — Login / refresh / logout

1. Login Estudante → dashboard.
2. Aguardar/forçar refresh de token (adapter).
3. Logout manual → nova visita a rota autenticada exige login.
4. Encerrar sessão no Keycloak / expirar → FE trata logout/redirect.

**OK**: sem acesso a API com token inválido; sem vazamento de senha em UI.

### V3 — Placeholder Criar tarefas

1. Como Estudante, abrir “Criar tarefas”.
2. Ver tela placeholder (sem CRUD).

**OK**: Admin não vê o item; rota direta Admin bloqueada.

### V4 — Admin cria / edita / desabilita / reabilita

1. Login Admin → Usuários.
2. Criar usuário com perfil selecionável e senha final.
3. Editar nome/formação (não e-mail/senha).
4. Desabilitar → login do alvo falha; Keycloak `enabled=false` + soft delete.
5. Reabilitar → login volta; estados sincronizados.

**OK**: SC-010, SC-011, SC-012.

### V5 — Alteração e-mail/senha

1. Estudante cria solicitação.
2. Admin aprova.
3. Estudante conclui com CPF correto + novos dados.
4. Login com novos dados.

**OK**: CPF errado ou sem aprovação bloqueia (SC-007).

### V6 — Mobile-first

1. Viewport ≤ 390px: login/cadastro, menu e dashboard utilizáveis sem scroll
   horizontal bloqueante.

## Testes automatizados mínimos (constituição)

```bash
cd backend && ./mvnw test
cd frontend && npm test
```

Cobrir no mínimo: validação CPF/unicidade, transições de
`LoginChangeRequest`, serviço disable/enable (mock Keycloak Admin), guards
de rota Angular por perfil.

## Checklist de segurança rápida

- [ ] Nenhum secret real em `environment*.ts`, compose de exemplo ou tema
- [ ] Senhas só no Keycloak; nunca em resposta JSON
- [ ] CPF mascarado em listagens quando adequado
- [ ] Rotas de negócio 401 sem JWT
