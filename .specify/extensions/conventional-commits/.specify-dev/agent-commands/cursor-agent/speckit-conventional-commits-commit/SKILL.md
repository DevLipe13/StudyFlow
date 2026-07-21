---
name: speckit-conventional-commits-commit
description: Um Conventional Commit por Phase do tasks.md; branch se em main; push
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: conventional-commits:commands/commit.md
---

## User Input

```text
$ARGUMENTS
```

Argumentos opcionais:

- lista de IDs (`T081 T082`) → só essas tasks entram, ainda **agrupadas por Phase**
- vazio → deriva tasks do `tasks.md` + diff, agrupa por Phase
- **não** passe uma mensagem única para “tudo” (cada Phase gera a própria)

## Objetivo

Criar **vários commits**, **um por Phase** do `tasks.md` (não um por task):

1. Localizar `tasks.md`
2. Mapear diff → tasks (`Txxx`) → **agrupar por Phase**
3. Se branch protegida: criar **uma** branch (pelo subject da **primeira** Phase) **antes** de qualquer commit
4. Para **cada Phase** (na ordem): stage arquivos daquela Phase → **um** Conventional Commit
5. Um único `git push -u origin HEAD` no final

Não use `--no-verify`, `--amend` (salvo protocolo do usuário) nem force push.

## Safety (obrigatório)

- Só execute porque este comando foi invocado.
- Nunca atualize `git config`.
- Nunca stage/commit de `.env`, credenciais, `*.pem`, etc.
- Mensagem via HEREDOC **por commit**.
- Nunca push direto em `main` / `homologacao` / `treinamento`.
- Em branch protegida: criar branch nova **antes** do primeiro commit.

## Config

Leia `.specify/extensions/conventional-commits/conventional-commits-config.yml` (ou template).

Defaults:

- `auto_stage: true`
- `auto_push: true`
- `message_from_tasks: true`
- `commit_grouping: phase`  ← um commit por Phase
- `protected_branches: [main, homologacao, treinamento]`

## Como funciona o agrupamento por Phase

No `tasks.md`, as tasks ficam sob cabeçalhos:

```markdown
## Phase 2: Foundational ...
- [x] T015 ...
- [x] T018 ...

## Phase 9: Convergence
- [x] T081 ...
- [x] T082 ...
```

Algoritmo:

1. Parseie o `tasks.md` de cima para baixo.
2. Cada vez que aparecer `## Phase N: ...`, a Phase atual passa a ser essa (guarde número `N` + título).
3. Cada linha `- [ ] Txxx` / `- [x] Txxx` **herda** a Phase atual (ID, descrição, paths em `` `...` ``).
4. Relacione arquivos do diff às tasks (paths citados / componente óbvio).
5. **Arquivo em 2+ tasks:** fica na task de **menor ID**; essa task define a Phase do arquivo.
6. Agrupe: `Phase → [tasks com arquivo pendente] → [arquivos]`.
7. Ignore Phases sem nenhum arquivo pendente.
8. Ordene as Phases pelo número (`Phase 1` antes de `Phase 9`).
9. **Um commit por Phase** restante.

Exemplo: diff toca T081, T082 (Phase 9) e T050 (Phase 6) → **2 commits** (Phase 6, depois Phase 9), não 3.

## Passos

### 1. Coletar estado git

```bash
git status
git branch --show-current
git diff
git diff --cached
git log --oneline -8
```

Ou:

```bash
.specify/extensions/conventional-commits/scripts/bash/collect-commit-context.sh
```

Sem mudanças → pare.

Se houver staged misturado: `git restore --staged .` antes do loop (não descarte working tree).

### 2. Localizar `tasks.md`

```bash
.specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks
```

Use `$FEATURE_DIR/tasks.md`.

Sem `tasks.md`: **um** commit único pelo diff (fallback) + branch/push; avise que não houve split por Phase.

### 3. Montar tasks e agrupar por Phase

1. Parseie Phases + tasks (seção “Como funciona o agrupamento”).
2. Se `$ARGUMENTS` tiver IDs → filtre só essas tasks (ainda agrupadas pela Phase de cada uma).
3. Senão → tasks cujos paths aparecem no diff/status.
4. Aplique regra de arquivo compartilhado (menor ID).
5. Produza lista ordenada: `(phaseNumber, phaseTitle, tasks[], files[])`.

### 4. Branch protegida (uma vez)

Antes do **primeiro** commit:

```bash
CURRENT="$(git branch --show-current)"
```

Se `CURRENT` ∈ `protected_branches`:

1. Gere o subject da **primeira** Phase do passo 5.3
2. Crie a branch:

```bash
BRANCH="$(.specify/extensions/conventional-commits/scripts/bash/subject-to-branch.sh "SUBJECT_PRIMEIRA_PHASE")"
git checkout -b "$BRANCH"
```

(Se já existir, sufixo `-2` / timestamp.)

### 5. Loop: um commit por Phase

Para cada Phase `P` na lista ordenada:

#### 5.1 Arquivos desta Phase

- União dos arquivos das tasks de `P` ainda pendentes
- Exclua segredos
- Lista vazia → pule `P`

#### 5.2 Stage só esses arquivos

```bash
git add -- path1 path2 ...
```

Confirme: `git diff --cached --stat` mostra **apenas** arquivos de `P`.

#### 5.3 Mensagem desta Phase

Subject (≤ 72, imperativo, sem ponto final):

```text
type(scope): complete Phase N short-title (T081-T083)
```

- `type` / `scope`: derive do conteúdo dominante da Phase (ex.: só testes → `test`; keycloak → `keycloak`)
- Intervalo de IDs: menor–maior na Phase; se poucos, liste `T081, T082`
- Resumo: pode usar o título curto da Phase (sem emoji)

Body (pt-BR se config disser):

```text
Phase: Phase 9: Convergence

Tasks:
- T081: <descrição curta>
- T082: <descrição curta>

Feature: <nome FEATURE_DIR>
```

Valide:

```bash
.specify/extensions/conventional-commits/scripts/bash/validate-message.sh "SUBJECT"
```

#### 5.4 Commit

```bash
git commit -m "$(cat <<'EOF'
type(scope): complete Phase 9 convergence (T081-T083)

Phase: Phase 9: Convergence

Tasks:
- T081: ...
- T082: ...

Feature: 001-auth-keycloak-dashboard

EOF
)"
```

Pre-commit falhou → corrija e **novo** commit (não `--amend`, salvo protocolo).

#### 5.5 Próxima Phase

Unstage residual se preciso; repita 5.1–5.4.

### 6. Push (uma vez no final)

Com `auto_push: true`:

```bash
git push -u origin HEAD
```

Garanta que `HEAD` não é branch protegida. Sem force push. Zero commits → não faça push.

### 7. Verificar

```bash
git status
git log --oneline -15
git branch -vv
```

## Saída

1. Branch (se criada)
2. Cada commit: hash + subject (Phase + range de Txxx)
3. Phases/tasks puladas (e por quê)
4. Resultado do push

Não abra PR a menos que o usuário peça.

## Exemplo esperado

Diff cobre T015, T018 (Phase 2) e T081, T082, T083 (Phase 9) → **2 commits**:

```text
a1b2c3d feat(backend): complete Phase 2 foundational (T015, T018)
e4f5g6h feat(keycloak): complete Phase 9 convergence (T081-T083)
```

Depois um push da branch com os dois.