---
name: speckit-conventional-commits-commit
description: Um Conventional Commit por task do tasks.md; branch se em main; push
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

- lista de IDs (`T081 T082`) → só essas tasks (nessa ordem)
- vazio → deriva tasks do `tasks.md` + diff
- **não** passe uma mensagem única para várias tasks (cada task gera a própria)

## Objetivo

Criar **vários commits**, **um por task** do `tasks.md`:

1. Localizar `tasks.md`
2. Mapear diff → lista ordenada de tasks (`Txxx`)
3. Se branch protegida: criar **uma** branch (pelo subject da **primeira** task) **antes** de qualquer commit
4. Para **cada** task, na ordem: stage **somente** os arquivos dela → commit Conventional Commit dela
5. Um único `git push -u origin HEAD` no final (com todos os commits)

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
- `commits_per_task: true`  ← um commit por task
- `protected_branches: [main, homologacao, treinamento]`

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

Se houver algo já staged misturado, `git restore --staged .` (ou unstage seletivo) **antes** de começar o loop — para não misturar arquivos de tasks diferentes. Não descarte working tree.

### 2. Localizar `tasks.md`

```bash
.specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks
```

Use `$FEATURE_DIR/tasks.md`.

Se não houver `tasks.md`: **um** commit único pelo diff (fallback) + branch/push como antes — e avise que não houve split por task.

### 3. Montar a lista de tasks (ordenada)

Leia o `tasks.md`. Extraia de cada linha `- [x]` / `- [ ]` o ID `Txxx`, descrição e paths citados.

Monte a lista assim:

1. Se `$ARGUMENTS` tiver IDs → essa lista, na ordem dada
2. Senão → tasks cujos paths aparecem no diff/status
3. Ordene pelo número do ID (`T081` antes de `T090`)
4. Prefira tasks que o diff claramente implementa (marcadas `[x]` ou arquivos novos/alterados batendo com a descrição)

Cada task da lista **deve** ter pelo menos um arquivo do working tree associado. Se uma task não tiver arquivos restantes (já commitados ou só docs já incluídos em outra), **pule** com aviso.

**Arquivo compartilhado por 2+ tasks:** inclua no commit da **menor** task ID da lista que o cita; nas seguintes, não resteage esse arquivo se já foi commitado.

### 4. Branch protegida (uma vez)

Antes do **primeiro** commit:

```bash
CURRENT="$(git branch --show-current)"
```

Se `CURRENT` ∈ `protected_branches`:

1. Gere o subject da **primeira** task (passo 5)
2. Crie a branch:

```bash
BRANCH="$(.specify/extensions/conventional-commits/scripts/bash/subject-to-branch.sh "SUBJECT_PRIMEIRA_TASK")"
git checkout -b "$BRANCH"
```

(Se já existir, sufixo `-2` / timestamp.)

### 5. Loop: um commit por task

Para cada task `T` na lista:

#### 5.1 Arquivos desta task

- Paths citados na linha da task no `tasks.md` que ainda estão modificados/untracked
- Arquivos do diff claramente só dessa task (mesmo diretório/componente descrito)
- Exclua segredos

Se a lista de arquivos desta task ficar vazia → pule `T`.

#### 5.2 Stage só esses arquivos

```bash
git add -- path1 path2 ...
```

Confirme: `git diff --cached --stat` mostra **apenas** os arquivos de `T`.

#### 5.3 Mensagem desta task

Subject (≤ 72, imperativo, sem ponto final):

```text
type(scope): <resumo da descrição da task> (T081)
```

Body (pt-BR se config disser):

```text
Tasks:
- T081: <descrição da task no tasks.md>

Feature: <nome FEATURE_DIR>
```

Valide:

```bash
.specify/extensions/conventional-commits/scripts/bash/validate-message.sh "SUBJECT"
```

#### 5.4 Commit

```bash
git commit -m "$(cat <<'EOF'
type(scope): resumo (T081)

Tasks:
- T081: ...

Feature: 001-auth-keycloak-dashboard

EOF
)"
```

Pre-commit falhou → corrija só o necessário dessa task e **novo** commit (não `--amend`, salvo protocolo).

#### 5.5 Próxima task

Unstage residual se sobrar algo staged (`git restore --staged` nos paths errados) e repita 5.1–5.4.

### 6. Push (uma vez no final)

Depois de **todos** os commits bem-sucedidos, com `auto_push: true`:

```bash
git push -u origin HEAD
```

Garanta que `HEAD` não é branch protegida. Sem force push.

Se nenhum commit foi criado → não faça push.

### 7. Verificar

```bash
git status
git log --oneline -15
git branch -vv
```

## Saída

Liste, em ordem:

1. Branch (se criada)
2. Cada commit: hash curto + subject (`… (T0xx)`)
3. Tasks puladas (e por quê)
4. Resultado do push

Não abra PR a menos que o usuário peça.

## Exemplo esperado

Diff cobre T081, T082, T083 → **3 commits**:

```text
a1b2c3d feat(keycloak): implement register.ftl fields (T081)
e4f5g6h feat(keycloak): show Estudante profile on register (T082)
i7j8k9l feat(keycloak): add realm protocol mappers (T083)
```

Depois um push da branch com os três.