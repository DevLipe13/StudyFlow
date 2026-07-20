# Feature Specification: Autenticação Keycloak e Dashboard Inicial

**Feature Branch**: `001-auth-keycloak-dashboard`

**Created**: 2026-07-15

**Status**: Draft

**Input**: User description: "Crie a estrutura do projeto como sendo backend e frontend separados. O projeto é um gerenciador de tarefas pessoal voltado para estudantes. A principio gere o fluxo de login e criação de conta utilizando keycloak. Alguns dados do usuario devem ser salvos na nossa base de dados como: nome, data de nascimento, formação (nivel escolar), o id do cadastro no keycloak e email. Além disso adicione informações extra como data de cadastro na base, data de atualização e soft delete. Ao logar exiba um dashboard vazio a principio e o menu."

## Clarifications

### Session 2026-07-15

- Q: Quais perfis a aplicação possui e o que o Admin pode fazer? → A: Perfis Estudante e Admin. Admin consulta e edita dados do usuário; cadastra novos usuários pela interface usando a API do Keycloak; alteração de e-mail/senha segue fluxo com aprovação e CPF (detalhado abaixo). Menu do Estudante inclui “Criar tarefas”; Admin não vê essa opção. Repositório inclui pasta dedicada ao tema Keycloak da aplicação.
- Q: Como funciona a confirmação de CPF na alteração de dados de login (e-mail e senha)? → A: Opção C — o estudante solicita a alteração; o Admin apenas aprova; em seguida o estudante confirma o próprio CPF e define os novos dados. Esse fluxo vale apenas para e-mail e senha (dados de login), não para os demais campos do perfil.
- Q: Onde o campo de perfil aparece no cadastro e quais valores são permitidos? → A: Cadastro no tema Keycloak e na aplicação MUST exibir campo de perfil. No tema Keycloak o perfil é sempre Estudante. Na plataforma (cadastro/gestão pelo Admin) é possível selecionar Admin ou Estudante.
- Q: O que “Criar tarefas” entrega nesta feature? → A: Opção A — item de menu do Estudante + tela placeholder (vazia/“em breve”), sem CRUD completo de tarefas nesta feature.
- Q: Como a senha é definida no cadastro de usuário pelo Admin? → A: Opção A — o Admin define a senha final no cadastro, sem troca obrigatória no primeiro login.
- Q: O perfil Admin também terá Dashboard no menu? → A: Sim — o menu do Admin MUST incluir Dashboard (além da gestão de usuários e Sair), com acesso ao dashboard inicial vazio após o login.
- Q: O Admin pode desabilitar um usuário? → A: Sim — o Admin pode desabilitar um usuário; o processo MUST desabilitar a identidade no Keycloak e aplicar a desabilitação/soft delete correspondente na base de dados do StudyFlow.
- Q: O Admin pode reabilitar um usuário desabilitado? → A: Opção B — sim; desabilitar e reabilitar nesta feature, ambos sincronizando Keycloak e base do StudyFlow.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Criar conta de estudante (Priority: P1)

Um estudante novo acessa o StudyFlow (tema Keycloak e/ou fluxo da
aplicação) e cria uma conta informando os dados pessoais necessários
(nome, e-mail, CPF, data de nascimento, formação/nível escolar, perfil) e
as credenciais de acesso. No auto-cadastro via tema Keycloak, o perfil é
sempre Estudante. A identidade é criada no Keycloak e o perfil
correspondente é gravado na base do StudyFlow, incluindo o identificador
do Keycloak, o e-mail e os metadados de auditoria.

**Why this priority**: Sem conta não há uso do produto; é o ponto de entrada
do fluxo pessoal do estudante.

**Independent Test**: Pode ser validado criando uma conta nova e confirmando
que o estudante consegue concluir o cadastro e que o perfil aparece na base
do StudyFlow vinculado ao identificador do Keycloak, com perfil Estudante,
sem precisar do dashboard completo.

**Acceptance Scenarios**:

1. **Given** um visitante sem conta, **When** ele preenche os dados obrigatórios
   de cadastro (incluindo perfil Estudante no tema Keycloak) e conclui a
   criação de conta, **Then** a identidade existe no Keycloak e um registro
   de usuário é criado na base do StudyFlow com nome, CPF, data de nascimento,
   formação (nível escolar), perfil Estudante, e-mail, id do Keycloak, data
   de cadastro e data de atualização, sem soft delete aplicado.
2. **Given** um visitante no formulário de cadastro, **When** ele omite um
   campo obrigatório ou informa e-mail/CPF já utilizado, **Then** o sistema
   impede a criação da conta e apresenta mensagem clara do problema, sem
   criar registro inconsistente na base do StudyFlow.
3. **Given** um cadastro concluído com sucesso, **When** a operação termina,
   **Then** o estudante é direcionado para o fluxo autenticado (dashboard
   inicial com menu) ou para o login, de forma consistente e sem expor
   senha ou segredos.

---

### User Story 2 - Entrar na conta e ver o dashboard inicial (Priority: P1)

Um usuário já cadastrado (Estudante ou Admin) faz login com suas credenciais
via Keycloak e, após autenticação bem-sucedida, visualiza o dashboard
inicial (ainda vazio de conteúdo de estudo) e o menu de navegação adequado
ao seu perfil.

**Why this priority**: É o fluxo diário de entrada; entrega a casa segura
após o login.

**Independent Test**: Com um usuário válido existente, realizar login e
confirmar que o dashboard vazio e o menu correto para o perfil são
exibidos; usuários inválidos ou excluídos logicamente não acessam.

**Acceptance Scenarios**:

1. **Given** um usuário com conta ativa (não soft-deleted), **When** ele
   informa credenciais válidas e conclui o login via Keycloak, **Then** ele
   acessa a área autenticada e vê o dashboard inicial vazio e o menu de
   navegação correspondente ao perfil (Estudante ou Admin).
2. **Given** um visitante na tela de login, **When** ele informa credenciais
   inválidas, **Then** o acesso é negado com mensagem compreensível e nenhum
   dado sensível (senha, tokens, CPF completo desnecessário) é exibido.
3. **Given** um usuário com registro soft-deleted na base do StudyFlow,
   **When** ele tenta login com credenciais ainda válidas no provedor de
   identidade, **Then** o acesso à área autenticada do StudyFlow é negado.

---

### User Story 3 - Navegar pelo menu conforme o perfil (Priority: P2)

Após o login, o usuário utiliza o menu para se orientar na aplicação. O
Estudante vê Dashboard, Criar tarefas e Sair. Ao tocar em Criar tarefas,
abre uma tela placeholder (sem criar/listar tarefas de verdade nesta
versão). O Admin vê Dashboard, opções de gestão de usuários e Sair — sem
a opção Criar tarefas.

**Why this priority**: Fecha o MVP visual mobile-first da área logada e
diferencia as jornadas por perfil.

**Independent Test**: Com sessão de Estudante e de Admin, abrir o menu e
confirmar itens visíveis distintos; no Estudante, abrir Criar tarefas e
ver apenas a placeholder; logout invalida acesso autenticado.

**Acceptance Scenarios**:

1. **Given** um Estudante autenticado, **When** ele abre o menu, **Then**
   visualiza Dashboard, Criar tarefas e Sair.
2. **Given** um Estudante autenticado, **When** ele escolhe Criar tarefas,
   **Then** vê uma tela placeholder indicando que a funcionalidade ainda
   não está disponível (ou equivalente), sem formulário de criação real.
3. **Given** um Admin autenticado, **When** ele abre o menu, **Then**
   visualiza Dashboard (obrigatório), opções de gestão de usuários e Sair,
   e NÃO visualiza Criar tarefas.
4. **Given** um Admin autenticado, **When** ele escolhe Dashboard no menu,
   **Then** visualiza o dashboard inicial vazio da área autenticada.
5. **Given** um usuário autenticado, **When** ele escolhe sair (logout),
   **Then** a sessão é encerrada e tentativas de reabrir o dashboard exigem
   novo login.
6. **Given** um usuário em viewport móvel, **When** ele usa o menu e o
   dashboard, **Then** a navegação permanece utilizável sem depender de
   layout exclusivo de desktop.

---

### User Story 4 - Admin gerencia usuários (Priority: P2)

Um Admin autenticado consulta e edita dados de perfil dos usuários
(campos que não são e-mail/senha), cadastra novos usuários pela interface
da plataforma (escolhendo perfil Admin ou Estudante, via Keycloak + base
local) e pode desabilitar e reabilitar usuários. Ambas as operações MUST
sincronizar Keycloak e a base do StudyFlow (soft delete / marca de
desabilitado e sua remoção).

**Why this priority**: Habilita operação administrativa sem depender só do
auto-cadastro.

**Independent Test**: Como Admin, listar um usuário, editar nome/formação
(ou campo de perfil permitido), cadastrar um novo usuário com perfil
selecionável, desabilitar e depois reabilitar um usuário, confirmando
bloqueio e restauração de login com estado coerente no Keycloak e na base
local.

**Acceptance Scenarios**:

1. **Given** um Admin autenticado, **When** ele consulta a lista ou o
   detalhe de um usuário, **Then** visualiza os dados de perfil permitidos
   sem ver senha ou segredos.
2. **Given** um Admin autenticado, **When** ele edita dados de perfil que
   não sejam e-mail nem senha e salva, **Then** as alterações são persistidas
   e a data de atualização é renovada.
3. **Given** um Admin autenticado no cadastro interno, **When** ele cria um
   novo usuário informando os campos obrigatórios, a senha final e
   selecionando perfil Admin ou Estudante, **Then** a identidade é criada
   no Keycloak com essa senha, o registro local é gravado com o perfil
   escolhido, e o novo usuário pode autenticar com a senha definida sem
   ser forçado a trocá-la no primeiro login.
4. **Given** um Admin autenticado e um usuário ativo, **When** o Admin
   desabilita esse usuário, **Then** a identidade fica desabilitada no
   Keycloak, o registro na base do StudyFlow fica desabilitado (soft
   delete / marca equivalente), a data de atualização é renovada, e o
   usuário não consegue mais acessar a área autenticada.
5. **Given** um usuário desabilitado, **When** ele tenta login com
   credenciais antes válidas, **Then** o acesso ao StudyFlow é negado.
6. **Given** um Admin autenticado e um usuário desabilitado, **When** o
   Admin reabilita esse usuário, **Then** a identidade volta a ficar
   habilitada no Keycloak, a marca de desabilitado/soft delete é removida
   na base, a data de atualização é renovada, e o usuário consegue
   autenticar novamente com credenciais válidas.
7. **Given** um não-Admin, **When** ele tenta acessar telas ou ações de
   gestão de usuários (incluindo desabilitar/reabilitar), **Then** o
   acesso é negado.

---

### User Story 5 - Alterar e-mail ou senha com aprovação e CPF (Priority: P2)

Alterações de e-mail ou senha (dados de login) seguem um fluxo controlado:
o estudante solicita; o Admin aprova; o estudante confirma o próprio CPF e
então define o novo e-mail e/ou a nova senha. Demais dados do perfil NÃO
usam esse fluxo.

**Why this priority**: Protege credenciais sem impedir o Admin de manter
dados cadastrais.

**Independent Test**: Solicitar alteração de senha (ou e-mail), aprovar como
Admin, confirmar CPF como estudante e concluir a troca; tentar concluir sem
aprovação ou com CPF incorreto e verificar bloqueio.

**Acceptance Scenarios**:

1. **Given** um Estudante autenticado, **When** ele solicita alteração de
   e-mail e/ou senha, **Then** a solicitação fica pendente de aprovação do
   Admin.
2. **Given** uma solicitação pendente, **When** o Admin aprova, **Then** o
   estudante pode prosseguir para confirmação de CPF e definição dos novos
   dados de login.
3. **Given** uma solicitação aprovada, **When** o estudante informa CPF
   correto e os novos dados de login válidos, **Then** a alteração é
   aplicada no provedor de identidade / base conforme o tipo (e-mail e/ou
   senha) e a data de atualização do registro local é renovada quando
   aplicável.
4. **Given** uma solicitação aprovada, **When** o estudante informa CPF
   incorreto, **Then** a alteração é recusada e os dados de login
   permanecem inalterados.
5. **Given** uma solicitação ainda não aprovada, **When** o estudante tenta
   concluir a alteração, **Then** a operação é bloqueada.

---

### Edge Cases

- Tentativa de cadastro com e-mail ou CPF já existente no Keycloak ou na
  base do StudyFlow.
- Cadastro parcialmente concluído (identidade criada no Keycloak, mas falha
  ao gravar o perfil local): o sistema MUST evitar estado “órfão” perceptível
  ao usuário ou oferecer recuperação clara, sem expor detalhes técnicos
  sensíveis.
- Login de usuário soft-deleted.
- Sessão expirada ao tentar acessar o dashboard.
- Acesso direto à URL do dashboard ou de gestão sem autenticação/autorização.
- Campos de data de nascimento inválidos, formação fora da lista permitida
  ou perfil inválido.
- Falha temporária do Keycloak durante login, cadastro ou alteração de
  credenciais.
- Solicitação de alteração de login rejeitada ou expirada (se houver prazo).
- Admin tenta alterar e-mail/senha de usuário fora do fluxo de solicitação +
  aprovação + CPF (incluindo definir nova senha direta no detalhe do usuário
  após o cadastro inicial).
- Estudante tenta acessar cadastro de usuários com seleção de perfil Admin.
- Admin tenta acessar diretamente a rota de Criar tarefas: acesso negado ou
  redirecionamento, sem exibir a experiência de estudante.
- Falha ao desabilitar ou reabilitar no Keycloak após alterar a base local
  (ou o inverso): o sistema MUST evitar estado divergente perceptível ou
  sinalizar falha sem deixar o usuário inconsistente de forma silenciosa.
- Admin tenta desabilitar a própria conta (se aplicável): comportamento
  definido no plano; não deve corromper a gestão do sistema.
- Tentativa de reabilitar usuário já ativo: operação idempotente ou mensagem
  clara, sem erro destructivo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O produto MUST ser estruturado como frontend e backend
  separados (aplicação cliente e serviço de API distintos), alinhado ao
  propósito de gerenciador de tarefas pessoais para estudantes.
- **FR-002**: O sistema MUST permitir criação de conta utilizando Keycloak
  como provedor de identidade.
- **FR-003**: O sistema MUST permitir login utilizando Keycloak para os
  perfis Estudante e Admin.
- **FR-004**: Durante o cadastro, o sistema MUST coletar e validar: nome,
  CPF, data de nascimento, formação (nível escolar), e-mail, perfil e as
  credenciais exigidas pelo fluxo de identidade.
- **FR-005**: Após criação bem-sucedida da identidade no Keycloak, o sistema
  MUST persistir na base do StudyFlow o perfil do usuário contendo no mínimo:
  nome, CPF, data de nascimento, formação (nível escolar), perfil
  (`estudante` ou `admin`), id do cadastro no Keycloak e e-mail.
- **FR-006**: O registro de usuário na base do StudyFlow MUST incluir data de
  cadastro, data de atualização e suporte a soft delete (exclusão lógica).
- **FR-007**: O sistema MUST atualizar a data de atualização sempre que dados
  persistidos do perfil forem alterados com sucesso.
- **FR-008**: Usuários desabilitados (soft delete / marca de desabilitado na
  base) MUST NÃO acessar a área autenticada do StudyFlow.
- **FR-009**: Após login bem-sucedido de usuário ativo, o sistema MUST exibir
  um dashboard inicial vazio (sem widgets ou listas de estudo nesta versão) e
  o menu de navegação conforme o perfil.
- **FR-010**: O menu do Estudante MUST incluir Dashboard, Criar tarefas e
  Sair (logout). O item Criar tarefas MUST abrir apenas uma tela placeholder
  (sem CRUD de tarefas nesta feature). O menu do Admin MUST incluir
  Dashboard (mesmo dashboard inicial vazio acessível via menu), acesso à
  gestão de usuários e Sair, e MUST NÃO incluir Criar tarefas.
- **FR-011**: Rotas e telas da área autenticada MUST exigir sessão válida;
  visitantes não autenticados MUST ser direcionados ao login. Ações de
  Admin MUST exigir perfil Admin.
- **FR-012**: Senhas, tokens e demais segredos MUST NÃO ser armazenados em
  texto claro na base do StudyFlow, nem expostos em interface, logs ou
  mensagens de erro; configuração do Keycloak e credenciais de integração
  MUST usar variáveis de ambiente.
- **FR-013**: Interfaces de login, cadastro, dashboard, menu e gestão MUST
  ser utilizáveis em viewport móvel como experiência primária (mobile-first).
- **FR-014**: A formação (nível escolar) MUST ser escolhida a partir de um
  conjunto controlado de valores adequados ao contexto educacional brasileiro
  (ex.: fundamental, médio, graduação, pós-graduação, outro), documentado nas
  Assumptions.
- **FR-015**: Documentação desta feature MUST estar em português brasileiro;
  regras de negócio e textos de interface MUST ser compreensíveis em
  português brasileiro.
- **FR-016**: O repositório MUST conter uma pasta dedicada ao tema Keycloak
  utilizado pela aplicação (login/cadastro customizados).
- **FR-017**: O cadastro no tema Keycloak MUST exibir campo de perfil e
  MUST fixar o valor como Estudante (sem permitir Admin nesse canal).
- **FR-018**: O cadastro/gestão de usuários na plataforma MUST exibir campo
  de perfil com seleção entre Admin e Estudante (destinado ao fluxo de
  Admin).
- **FR-019**: O Admin MUST poder consultar e editar dados de perfil do
  usuário que não sejam e-mail nem senha.
- **FR-020**: O Admin MUST poder cadastrar novos usuários pela interface do
  sistema, criando a identidade via API do Keycloak e o registro local
  correspondente, definindo a senha final no momento do cadastro (sem
  exigir troca obrigatória no primeiro login).
- **FR-021**: Alterações de e-mail e/ou senha MUST seguir exclusivamente o
  fluxo: solicitação pelo estudante → aprovação pelo Admin → confirmação do
  CPF pelo estudante → aplicação da alteração. Demais campos do perfil MUST
  NÃO exigir esse fluxo.
- **FR-022**: CPF MUST ser único entre usuários ativos e MUST ser exigido no
  cadastro para viabilizar a confirmação no fluxo de alteração de login.
- **FR-023**: O Admin MUST poder desabilitar um usuário pela interface de
  gestão. A desabilitação MUST (1) desabilitar a conta no Keycloak e (2)
  aplicar desabilitação/soft delete na base do StudyFlow, mantendo
  consistência entre os dois lados.
- **FR-024**: O Admin MUST poder reabilitar um usuário desabilitado pela
  interface de gestão. A reabilitação MUST (1) reabilitar a conta no
  Keycloak e (2) remover a desabilitação/soft delete na base do StudyFlow,
  mantendo consistência entre os dois lados.

### Key Entities

- **Usuário (User)**: Representa Estudante ou Admin no StudyFlow. Atributos
  de negócio: nome, CPF, data de nascimento, formação (nível escolar),
  perfil (`estudante` | `admin`), e-mail, id do cadastro no Keycloak, data
  de cadastro, data de atualização, indicador/marca de soft delete
  (desabilitado). Relaciona-se 1:1 com a identidade no Keycloak via id do
  Keycloak; desabilitação/reabilitação implicam o mesmo estado (desabilitado
  ou ativo) no Keycloak e na base local.
- **Solicitação de alteração de login**: Pedido do estudante para mudar
  e-mail e/ou senha; estados mínimos: pendente, aprovada, concluída,
  rejeitada (conforme implementação de negócio). Exige aprovação do Admin e
  confirmação de CPF do solicitante antes de concluir.
- **Sessão autenticada**: Estado de acesso após login bem-sucedido via
  Keycloak; habilita dashboard e menu do perfil até logout ou expiração.
- **Menu de navegação**: Estrutura da área autenticada; itens variam por
  perfil (Estudante: Dashboard, Criar tarefas → placeholder, Sair; Admin:
  Dashboard, gestão de usuários, Sair).
- **Tema Keycloak**: Pacote visual/fluxo de login e cadastro hospedado em
  pasta dedicada no repositório; cadastro nesse canal sempre com perfil
  Estudante.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Um estudante novo consegue criar conta e chegar à área
  autenticada (ou conclusão explícita do cadastro) em menos de 3 minutos em
  condições normais de rede.
- **SC-002**: Um usuário com conta ativa consegue fazer login e visualizar
  o dashboard vazio com o menu do seu perfil em menos de 1 minuto.
- **SC-003**: Em testes manuais do fluxo feliz, 100% dos cadastros válidos
  resultam em perfil local com todos os campos obrigatórios preenchidos
  (incluindo CPF e perfil) e vinculados ao id do Keycloak.
- **SC-004**: Em rotas autenticadas, 100% das tentativas sem sessão válida
  são bloqueadas; 100% das ações de Admin tentadas por Estudante são
  bloqueadas, sem vazar dados sensíveis.
- **SC-005**: Em viewport móvel representativo (largura ≤ 390px), um
  usuário consegue concluir login ou cadastro e abrir o menu sem scroll
  horizontal bloqueante nem controles inacessíveis.
- **SC-006**: Nenhuma senha ou segredo de integração aparece em telas de
  erro, respostas visíveis ao usuário ou documentação de exemplo com valores
  reais.
- **SC-007**: Em 100% dos testes do fluxo de alteração de e-mail/senha, a
  mudança só ocorre após solicitação + aprovação do Admin + CPF correto; CPF
  incorreto ou ausência de aprovação impede a mudança.
- **SC-008**: Em verificação de menu, 100% das sessões de Estudante mostram
  Dashboard e Criar tarefas; 100% das sessões de Admin mostram Dashboard e
  não mostram Criar tarefas.
- **SC-009**: Ao abrir Criar tarefas como Estudante, 100% das vezes a
  experiência é apenas placeholder — nenhuma tarefa é criada ou listada
  nesta feature.
- **SC-010**: Usuário criado pelo Admin com senha final consegue autenticar
  com essa senha na primeira tentativa, sem etapa obrigatória de troca de
  senha.
- **SC-011**: Após o Admin desabilitar um usuário, em 100% dos testes do
  fluxo feliz o usuário deixa de autenticar no StudyFlow e o estado
  desabilitado está refletido no Keycloak e na base local.
- **SC-012**: Após o Admin reabilitar um usuário desabilitado, em 100% dos
  testes do fluxo feliz o usuário volta a autenticar e o estado ativo está
  refletido no Keycloak e na base local.

## Assumptions

- O público-alvo principal são estudantes que acessam pelo celular; Admin
  também usa a mesma aplicação com menu distinto.
- Auto-cadastro público via tema Keycloak permanece disponível e gera apenas
  perfil Estudante; criação de Admin ocorre na plataforma pelo Admin.
- No cadastro pela plataforma, o Admin define a senha final do novo usuário
  (sem force-update no primeiro login). Após o cadastro, mudanças de e-mail/
  senha seguem o fluxo de solicitação + aprovação + CPF.
- “Criar tarefas” nesta feature = item de menu + tela placeholder; CRUD de
  tarefas fica para feature futura.
- Gestão de matérias, prazos e sessões de estudo permanece fora do escopo
  desta feature.
- Keycloak é o único provedor de identidade nesta fase.
- Valores iniciais de formação (nível escolar): `fundamental`, `medio`,
  `graduacao`, `posGraduacao`, `outro` (rótulos em português na interface).
- Valores de perfil: `estudante`, `admin`.
- Desabilitar/reabilitar usuário = soft delete (ou remoção da marca) na base
  + desabilitar/reabilitar a conta no Keycloak, feito pelo Admin. UI de
  autoexclusão do próprio usuário fica para feature futura.
- Bootstrap do primeiro Admin (seed/manual) fica para o plano de implementação.
- Credenciais e parâmetros de conexão com Keycloak e com a base de dados
  vêm exclusivamente de variáveis de ambiente.
- Frontend e backend separados compartilham contratos de API estáveis; o
  frontend não acessa a base de dados diretamente.
- Em falha entre criação no Keycloak e gravação local, prioriza-se evitar
  conta utilizável inconsistente e orientar o usuário a tentar novamente,
  sem expor stack traces ou segredos.
- CPF é armazenado de forma adequada à privacidade (exibição mascarada quando
  possível) e usado na confirmação do fluxo de login; não é exibido em logs.
