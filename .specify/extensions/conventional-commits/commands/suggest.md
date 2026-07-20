---
description: "Sugere uma mensagem Conventional Commit por task do tasks.md (sem commitar)"
---

## User Input

```text
$ARGUMENTS
```

IDs opcionais (`T081 T082`). Se vazio, deriva do `tasks.md` + diff.

## Objetivo

Mostrar **uma mensagem por task** (preview do que `/speckit-conventional-commits-commit` faria). **Não** execute `git commit`, `git add` nem `git push`.

## Passos

1. Confirme git repo e colete contexto com `collect-commit-context.sh`.
2. Localize `tasks.md` via `check-prerequisites.sh --json --require-tasks --include-tasks`.
3. Monte a lista de tasks (mesmas regras do commit: IDs em `$ARGUMENTS` ou paths do diff).
4. Para **cada** task, proponha:
   - arquivos que entrariam naquele commit
   - subject `type(scope): resumo (T0xx)`
   - body com `Tasks:` + `Feature:`
5. Valide cada subject com `validate-message.sh`.

## Saída

Liste os commits previstos em ordem (1 bloco por task). Depois sugira:

`/speckit-conventional-commits-commit` para executar (um commit por task + push).
