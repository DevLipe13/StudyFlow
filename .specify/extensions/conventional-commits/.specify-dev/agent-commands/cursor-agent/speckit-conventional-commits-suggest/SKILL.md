---
name: speckit-conventional-commits-suggest
description: Sugere uma mensagem Conventional Commit por Phase do tasks.md (sem commitar)
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: conventional-commits:commands/suggest.md
---

## User Input

```text
$ARGUMENTS
```

IDs opcionais (`T081 T082`). Se vazio, deriva do `tasks.md` + diff.

## Objetivo

Mostrar **uma mensagem por Phase** (preview do commit). **Não** execute `git commit`, `git add` nem `git push`.

## Passos

1. Colete contexto com `collect-commit-context.sh`.
2. Localize `tasks.md` via `check-prerequisites.sh --json --require-tasks --include-tasks`.
3. Parseie Phases → tasks; mapeie diff; **agrupo por Phase** (mesmas regras de `/speckit-conventional-commits-commit`).
4. Para **cada Phase** com arquivos, proponha:
   - lista de tasks (IDs) e arquivos
   - subject `type(scope): complete Phase N … (T0xx-T0yy)`
   - body com `Phase:` + `Tasks:` + `Feature:`
5. Valide cada subject com `validate-message.sh`.

## Saída

Liste 1 bloco por Phase. Sugira `/speckit-conventional-commits-commit` para executar.