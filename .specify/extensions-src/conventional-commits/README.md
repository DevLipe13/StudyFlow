# Conventional Commits (Spec Kit)

Extensão local do StudyFlow para Conventional Commits alinhados ao `tasks.md`.

## Comandos

| Slash command | Função |
|---|---|
| `/speckit-conventional-commits-suggest` | Preview: **1 mensagem por Phase** |
| `/speckit-conventional-commits-commit` | **1 commit por Phase** → branch se main → push |
| `/speckit-conventional-commits-validate` | Valida subject |

## Agrupamento (padrão: `commit_grouping: phase`)

1. Lê cabeçalhos `## Phase N: ...` no `tasks.md`
2. Cada task `Txxx` herda a Phase em que está
3. Arquivos do diff → tasks → Phases
4. **Um commit por Phase** que tiver arquivos pendentes

Exemplo: T081+T082+T083 (Phase 9) → 1 commit, não 3.

Outros modos no config: `task` | `single`.

## Instalação

```bash
specify extension add --dev --force .specify/extensions-src/conventional-commits
```
