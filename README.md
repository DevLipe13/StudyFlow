# StudyFlow

Gerenciador de tarefas pessoal voltado para estudantes.

## Estrutura

- `frontend/` — Angular 19 (TypeScript, signals, Keycloak JS)
- `backend/` — Quarkus (Java 21, Hibernate, Flyway, OIDC, Keycloak Admin)
- `keycloak/` — tema customizado `studyflow` e realm de exemplo
- `specs/` — especificação e plano Spec Kit

## Pré-requisitos

- JDK 21+, Maven 3.9+ (ou `backend/mvnw`)
- Node.js 22+
- Docker Compose
- Copie `.env.example` para `.env` e ajuste placeholders (sem secrets reais no git)

## Subir infraestrutura

```bash
docker compose up -d
```

Keycloak: http://localhost:8080 (tema em `keycloak/themes/studyflow`)  
Postgres: `localhost:5432`

Configure o realm (import `keycloak/realm/studyflow-realm.json` ou via UI) e troque os secrets de exemplo.

## Backend

```bash
cd backend
./mvnw quarkus:dev
```

API em http://localhost:8081 — rotas de negócio exigem JWT Bearer.

```bash
./mvnw test
```

## Frontend

```bash
cd frontend
npm install
npm start
```

App em http://localhost:4200. Environments em `src/environments/` (`homologacao`, `treinamento`, `production`).

```bash
npm test -- --watch=false --browsers=ChromeHeadless
```

## Perfis

- **Estudante**: Dashboard, Criar tarefas (placeholder), Sair
- **Admin**: Dashboard, Usuários, Sair (sem Criar tarefas)

Documentação da feature: `specs/001-auth-keycloak-dashboard/`.
