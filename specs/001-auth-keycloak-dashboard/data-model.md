# Data Model: Autenticação Keycloak e Dashboard Inicial

**Feature**: `001-auth-keycloak-dashboard` | **Date**: 2026-07-15

Convenções: atributos em **camelCase** no código (Java/TS). Colunas SQL em
`snake_case` via naming strategy do Hibernate (exceção de framework
documentada). Soft delete: `deletedAt` nulo = ativo.

## Enums

### ProfileType
| Value | Meaning |
|-------|---------|
| `estudante` | Perfil Estudante |
| `admin` | Perfil Admin |

### EducationLevel
| Value | UI (pt-BR) |
|-------|------------|
| `fundamental` | Fundamental |
| `medio` | Médio |
| `graduacao` | Graduação |
| `posGraduacao` | Pós-graduação |
| `outro` | Outro |

### LoginChangeType
| Value | Meaning |
|-------|---------|
| `email` | Alterar somente e-mail |
| `password` | Alterar somente senha |
| `emailAndPassword` | Ambos |

### LoginChangeStatus
| Value | Meaning |
|-------|---------|
| `pending` | Aguardando Admin |
| `approved` | Admin aprovou; aguarda CPF + conclusão |
| `rejected` | Admin rejeitou |
| `completed` | Alteração aplicada |

## Entities

### User (table: `app_user`)

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | UUID | PK | Surrogate |
| `keycloakId` | String | UNIQUE, NOT NULL | `sub` do Keycloak |
| `name` | String | NOT NULL | Nome completo |
| `email` | String | NOT NULL | Único entre ativos |
| `cpf` | String | NOT NULL | Único entre ativos; só dígitos normalizados |
| `birthDate` | LocalDate | NOT NULL | |
| `educationLevel` | EducationLevel | NOT NULL | |
| `profile` | ProfileType | NOT NULL | `estudante` \| `admin` |
| `createdAt` | Instant | NOT NULL | Set on insert |
| `updatedAt` | Instant | NOT NULL | Touch on update |
| `deletedAt` | Instant | NULL | Soft delete / disabled; null = ativo |

**Validation**:
- E-mail formato válido
- CPF 11 dígitos com validação de dígitos verificadores
- Unicidade de `email` e `cpf` apenas onde `deletedAt IS NULL`
- Perfil no auto-cadastro (tema) sempre `estudante`

**Relationships**:
- 1:N com `LoginChangeRequest` (como solicitante)
- Admin aprovador referenciado opcionalmente em `LoginChangeRequest.reviewedByUserId`

**State**:
- Ativo: `deletedAt == null` e Keycloak `enabled=true`
- Desabilitado: `deletedAt != null` e Keycloak `enabled=false`

### LoginChangeRequest (table: `login_change_request`)

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | UUID | PK | |
| `requesterUserId` | UUID | FK → app_user, NOT NULL | Estudante |
| `changeType` | LoginChangeType | NOT NULL | |
| `status` | LoginChangeStatus | NOT NULL | default `pending` |
| `proposedEmail` | String | NULL | Obrigatório se tipo inclui e-mail |
| `reviewedByUserId` | UUID | FK → app_user, NULL | Admin |
| `reviewedAt` | Instant | NULL | |
| `completedAt` | Instant | NULL | |
| `createdAt` | Instant | NOT NULL | |
| `updatedAt` | Instant | NOT NULL | |
| `rejectionReason` | String | NULL | Opcional |

**Notes**:
- Nova senha NÃO é armazenada; enviada apenas na conclusão (HTTPS) para
  Keycloak Admin API e descartada
- Confirmação de CPF é validada contra `User.cpf` no momento da conclusão
  (não armazenar CPF novamente na request)

**Transitions**:

```text
pending → approved (Admin)
pending → rejected (Admin)
approved → completed (Estudante + CPF ok + Keycloak ok)
```

## Flyway initial versions (planned)

| File | Purpose |
|------|---------|
| `V1__create_app_user.sql` | Tabela `app_user` + índices únicos parciais |
| `V2__create_login_change_request.sql` | Tabela + FKs + índices por status |

## Non-persisted concepts

- **Authenticated session**: JWT Keycloak no frontend (adapter); backend só
  valida bearer — sem tabela de sessão local
- **Menu items**: regra de UI (Estudante: Dashboard, Criar tarefas,
  Sair; Admin: Dashboard, Usuários, Sair)
