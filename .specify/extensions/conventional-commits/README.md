# Conventional Commits (Spec Kit)

Extensão local do StudyFlow para automatizar [Conventional Commits](https://www.conventionalcommits.org/) no fluxo Spec Kit, conforme a constituição do projeto.

## Comandos

| Slash command | Função |
|---|---|
| `/speckit-conventional-commits-suggest` | Só gera a mensagem (não commita) |
| `/speckit-conventional-commits-commit` | Gera mensagem → stage → branch se em `main` → commit → push |
| `/speckit-conventional-commits-validate` | Valida uma mensagem com o script |

## Fluxo de `/speckit-conventional-commits-commit`

1. Localiza `tasks.md` da feature
2. Monta a lista de tasks relacionadas ao diff (`Txxx`)
3. Se em branch protegida: cria **uma** branch (pela 1ª task)
4. **Um commit por task** — stage só dos arquivos daquela task
5. Um `git push -u origin HEAD` no final

Exemplo (3 tasks → 3 commits):

```text
feat(keycloak): implement register.ftl fields (T081)
feat(keycloak): show Estudante profile on register (T082)
feat(keycloak): add realm protocol mappers (T083)
```

## Scripts

- `scripts/bash/collect-commit-context.sh` — status, diffs e log
- `scripts/bash/validate-message.sh` — valida o subject
- `scripts/bash/subject-to-branch.sh` — subject → nome de branch

## Config

```bash
cp .specify/extensions/conventional-commits/conventional-commits-config.template.yml \
   .specify/extensions/conventional-commits/conventional-commits-config.yml
```

Campos importantes: `auto_stage`, `auto_push`, `protected_branches`.

## Instalação (dev)

```bash
specify extension add --dev --force .specify/extensions-src/conventional-commits
```
