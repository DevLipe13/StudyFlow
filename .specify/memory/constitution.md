<!--
Sync Impact Report:
- Version change: (template unfilled) → 1.0.0
- Modified principles:
  - [PRINCIPLE_1_NAME] → I. Desenvolvimento Mobile-First
  - [PRINCIPLE_2_NAME] → II. Clean Code e SOLID
  - [PRINCIPLE_3_NAME] → III. KISS (Simplicidade Estrita)
  - [PRINCIPLE_4_NAME] → IV. Segurança de Dados Sensíveis
  - [PRINCIPLE_5_NAME] → V. Qualidade com Testes Unitários
- Added sections:
  - Linguagem e Nomenclatura
  - Fluxo de Trabalho
  - Governance (conteúdo concreto)
- Removed sections: nenhuma (placeholders do template substituídos)
- Templates requiring updates:
  - .specify/templates/plan-template.md ✅ updated
  - .specify/templates/spec-template.md ✅ updated
  - .specify/templates/tasks-template.md ✅ updated
  - .cursor/skills/speckit-*/SKILL.md ✅ verified (sem referências obsoletas)
  - README.md ✅ verified (já em pt-BR; sem alinhamento de princípios necessário)
- Follow-up TODOs: nenhum
-->

# StudyFlow Constitution

## Core Principles

### I. Desenvolvimento Mobile-First
Interfaces e experiências de usuário MUST ser concebidas e validadas primeiro
para viewports móveis, e só então adaptadas progressivamente para telas maiores.
Layouts, tipografia, interações e desempenho MUST priorizar o uso em
dispositivos móveis. Decisões de UI/UX que degradam a experiência móvel em
favor do desktop são proibidas sem justificativa documentada no plano da feature.

**Rationale**: O StudyFlow é voltado a estudantes que estudam em contextos
móveis; a experiência primária MUST refletir esse uso real.

### II. Clean Code e SOLID
O código MUST seguir práticas de Clean Code e os princípios SOLID para
garantir design limpo, modular e de fácil manutenção. Responsabilidades MUST
ser claramente separadas; abstrações MUST ser estáveis e mínimas; módulos
MUST ser testáveis de forma isolada. Código duplicado, funções monolíticas e
acoplamento desnecessário MUST ser evitados ou refatorados antes do merge.

**Rationale**: Modularidade e clareza reduzem custo de evolução e facilitam
revisão em Pull Request.

### III. KISS (Simplicidade Estrita)
A equipe MUST adotar estritamente o princípio KISS: a solução escolhida MUST
ser sempre a mais simples e legível que atenda ao requisito. Complexidade
adicional (padrões extras, camadas, abstrações ou dependências) MUST ser
justificada no Constitution Check do plano. Em conflito entre elegância
teórica e legibilidade prática, prevalece a legibilidade.

**Rationale**: Simplicidade acelera entrega, revisão e onboarding sem
comprometer o valor entregue.

### IV. Segurança de Dados Sensíveis
É proibido expor dados sensíveis — incluindo, sem limitação, senhas de
usuários, tokens, chaves de API e segredos de infraestrutura — em código-fonte,
logs, respostas de API, mensagens de erro, commits ou artefatos versionados.
Credenciais e segredos MUST ser fornecidos exclusivamente via variáveis de
ambiente (ou mecanismo equivalente de secrets management aprovado). Valores
padrão com credenciais reais em arquivos de exemplo são proibidos; exemplos
MUST usar placeholders não funcionais.

**Rationale**: Exposição de segredos é risco crítico e irreversível após
commit público ou vazamento de logs.

### V. Qualidade com Testes Unitários
Toda nova lógica de negócio, regra de domínio ou comportamento não trivial
MUST incluir cobertura de testes unitários correspondente antes do merge.
Pull Requests que introduzam lógica nova sem testes unitários MUST ser
rejeitados. Testes MUST ser determinísticos, isolados e nomeados de forma
descritiva em inglês, alinhados ao código.

**Rationale**: Testes unitários protegem regressões e documentam comportamento
esperado da lógica crítica.

## Linguagem e Nomenclatura

Documentação do projeto (README, specs, planos, tasks, guias e comentários de
governança voltados a pessoas) MUST ser escrita em português brasileiro.
Código-fonte (identificadores, mensagens técnicas internas de log quando
destinadas a desenvolvedores, nomes de arquivos de código e testes) MUST
estar em inglês.

Atributos, propriedades e campos de dados no código MUST usar camelCase
(ex.: `studySession`, `dueDate`). Convenções específicas de frameworks que
imponham outro estilo MUST ser documentadas na feature e limitadas ao
mínimo necessário.

## Fluxo de Trabalho

Commits MUST seguir [Conventional Commits](https://www.conventionalcommits.org/)
(ex.: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`).

A branch `main` é a fonte da verdade do produto. O repositório MUST manter
também as branches dedicadas `homologacao` (ambiente de homologação) e
`treinamento` (ambiente de treinamento). Integração de demandas MUST ocorrer
exclusivamente via Pull Request; push direto de trabalho de feature em
`main`, `homologacao` ou `treinamento` é proibido. Features MUST nascer em
branches derivadas e ser integradas somente após revisão e aprovação do PR.

## Governance

Esta constituição prevalece sobre práticas informais, preferências pessoais e
atalhos locais. Em conflito entre conveniência e princípio normativo (MUST),
o princípio prevalece.

Emendas MUST:
1. Atualizar `.specify/memory/constitution.md` com versão e datas corretas.
2. Propagar impactos para templates Spec Kit e documentação afetada.
3. Registrar Sync Impact Report no topo do arquivo.
4. Ser propostas e integradas via Pull Request.

Versionamento semântico da constituição:
- **MAJOR**: remoção ou redefinição incompatível de princípios.
- **MINOR**: novo princípio/seção ou expansão material de orientação.
- **PATCH**: esclarecimentos, redação e correções sem mudança semântica.

Reviews de PR e gates de planejamento MUST verificar conformidade com os
princípios acima. Violações de MUST bloqueiam merge até correção ou emenda
formal da constituição. Complexidade além do KISS MUST ser justificada na
seção Complexity Tracking do plano da feature.

**Version**: 1.0.0 | **Ratified**: 2026-07-15 | **Last Amended**: 2026-07-15
