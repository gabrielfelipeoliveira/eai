# Regras de Negocio

Este documento e a fonte oficial para regras de negocio. Ele nao deve inventar comportamento. Quando uma regra nao estiver definida, a decisao deve ser registrada como pendencia para o Product Owner.

## Fontes

As regras conhecidas foram extraidas da documentacao e da implementacao atuais:

- `README.md`
- `docs/architecture.md`
- `docs/development-guide.md`
- `docs/email-importer.md`
- Codigo atual de dominio e aplicacao do backend
- Migrations atuais do banco

## Tenancy

Regras conhecidas:

- A plataforma suporta multiplas empresas.
- Uma empresa pode ter uma ou mais lojas.
- Lojas pertencem a uma empresa.
- Usuarios podem estar vinculados a uma empresa e loja.
- A visibilidade comercial e escopada por papel e tenant.
- A loja usada por um registro deve pertencer a mesma empresa.

Conflito aberto:

- A documentacao atual afirma que um `MANAGER` pode ser escopado por empresa e, se vinculado a uma loja especifica, limitado a essa loja.
- Alguns servicos de aplicacao atuais exigem que usuarios nao-admin tenham `storeId`, enquanto outros permitem que um manager sem `storeId` acesse dados escopados por empresa.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Um manager pode operar em todas as lojas da empresa quando `storeId` estiver ausente?
- Todo usuario nao-admin deve sempre estar vinculado a empresa e loja?
- Receptionists e auditors devem ter escopo por empresa ou por loja?

## Papeis e Permissoes

Papeis conhecidos:

- `ADMIN`
- `MANAGER`
- `SELLER`
- `RECEPTIONIST`
- `AUDITOR`

Regras conhecidas:

- `ADMIN` pode gerenciar empresas, lojas, usuarios, vinculos de tenant de usuarios e todos os leads.
- `MANAGER` pode visualizar usuarios e gerenciar lojas dentro do seu escopo de empresa, conforme a documentacao atual.
- `SELLER` e escopado a propria loja.
- Endpoints de gestao de leads permitem acesso a `ADMIN`, `MANAGER` e `SELLER`, com validacoes de tenant nos servicos.
- Algumas telas e rotas sao ocultadas no frontend com base no papel.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- O que `RECEPTIONIST` pode fazer?
- O que `AUDITOR` pode fazer?
- `MANAGER` pode criar usuarios ou apenas visualiza-los?
- `SELLER` pode ver leads nao atribuidos da loja?
- `SELLER` pode ver leads atribuidos a outros vendedores da mesma loja?

## Ciclo de Vida do Lead

Status conhecidos:

- `NEW`
- `AVAILABLE`
- `ASSIGNED`
- `FIRST_CONTACT`
- `IN_NEGOTIATION`
- `VISIT_SCHEDULED`
- `PROPOSAL_SENT`
- `SOLD`
- `LOST`
- `DUPLICATED`

Regras conhecidas:

- Leads manuais atualmente iniciam como `AVAILABLE`.
- Leads automaticos ou nao manuais atualmente iniciam como `NEW`.
- Um lead nao manual criado com usuario atribuido atualmente inicia como `ASSIGNED`.
- Atribuir um lead define o usuario responsavel, registra o momento da atribuicao e muda o status para `ASSIGNED`.
- Alterar um lead para `FIRST_CONTACT` define a data de primeiro contato quando ela estiver vazia.
- Status relacionados a contato atualizam a data de ultimo contato.
- Mudancas de status e atribuicoes criam registros de historico.
- A listagem de leads e paginada e suporta filtros por status, origem, vendedor, loja, periodo, texto, veiculo e telefone.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais transicoes de status sao permitidas?
- Quais transicoes de status sao proibidas?
- Um lead vendido ou perdido pode ser reaberto?
- Um lead duplicado pode voltar ao funil?
- Atribuir um lead manual sempre deve move-lo para `ASSIGNED`?
- Autoatribuicao deve ficar restrita a leads `NEW` ou `AVAILABLE`?
- Quais campos sao obrigatorios para cada origem de lead?

## Atribuicao de Leads

Regras conhecidas:

- Dono manual do lead por vendedor e suportado.
- Atribuicao por manager e suportada.
- Atribuicao automatica suporta `ROUND_ROBIN` e `LEAST_BUSY`.
- Atribuicao automatica considera usuarios ativos com papel `SELLER` na loja do lead.
- `ROUND_ROBIN` atribui ao proximo vendedor ativo depois da atribuicao mais recente da loja.
- `LEAST_BUSY` atribui ao vendedor ativo com menos leads em aberto.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Managers podem atribuir leads a si mesmos?
- Atribuicao deve considerar horario de trabalho ou disponibilidade do vendedor?
- Atribuicao deve considerar origem do lead ou especialidade do vendedor?
- O que deve acontecer quando nao houver vendedor elegivel?
- Um lead pode ser reatribuido livremente depois do primeiro contato?

## SLA

Regras conhecidas:

- Politica de SLA e escopada por loja.
- Um lead esta atrasado para atribuicao quando permanece sem vendedor responsavel depois de `minutesToAssign`.
- Um lead esta atrasado para primeiro contato quando tem vendedor responsavel, nao tem `firstContactAt`, e o limite configurado passou desde a atribuicao.
- Respostas da API de lead atualmente expõem indicadores calculados de SLA.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- O SLA conta tempo corrido ou horario comercial?
- O SLA pausa em algum status do lead?
- Quais usuarios podem editar a politica de SLA?
- Violacoes de SLA devem criar notificacoes ou tarefas?
- Qual e o fluxo de escalonamento para leads atrasados?

## Follow-Ups

Regras conhecidas:

- Tarefas de follow-up sao vinculadas a um lead e a um usuario responsavel.
- Tarefas de follow-up podem ser criadas, concluidas e canceladas.
- Uma tarefa pendente vencida expoe status efetivo de atraso.
- Criar, concluir e cancelar follow-ups registra historico no lead.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Managers podem criar follow-ups para vendedores?
- Vendedores podem concluir follow-ups criados por managers?
- Follow-ups devem gerar lembretes ou notificacoes?
- Follow-ups podem ser reagendados?

## Notas, Tags e Historico

Regras conhecidas:

- Leads podem ter notas.
- Leads podem ter tags.
- Leads mantem historico de status.
- Historicos armazenam status anterior, novo status, descricao, usuario e data de criacao.
- Registros de historico criados por scheduler podem ser registros de sistema sem usuario, conforme documentacao do importador de e-mail.

Conflito aberto:

- A migration atual `V4__create_leads.sql` define `lead_history.user_id` como `NOT NULL`.
- A documentacao do importador de e-mail afirma que entradas de historico criadas por scheduler usam um registro de sistema sem usuario.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Notas de lead sao editaveis ou imutaveis?
- Tags sao globais, escopadas por loja ou texto livre por lead?
- Historico deve suportar atores de sistema explicitamente?

## Templates de Mensagem e Links de WhatsApp

Regras conhecidas:

- Templates sao escopados por loja.
- Templates podem estar ativos ou inativos.
- Templates suportam placeholders para cliente, telefone, veiculo, vendedor, loja e cidade.
- Geracao de link de WhatsApp renderiza um template ativo da loja do lead.
- Geracao de link de WhatsApp registra uma comunicacao do lead.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Gerar link de WhatsApp deve marcar automaticamente o primeiro contato?
- O historico de comunicacao deve guardar apenas links gerados ou conversas reais futuramente?
- Templates podem ser da empresa inteira ou somente da loja?
- Quem pode criar, editar, desativar ou apagar templates?

## Importador de Leads por E-mail

Regras conhecidas:

- Contas de e-mail sao escopadas por loja.
- Apenas IMAP e suportado atualmente.
- O job automatico fica desativado por padrao.
- Sincronizacao manual e suportada.
- O parser generico extrai nome, telefone, e-mail, veiculo, mensagem original e origem quando possivel.
- Leads importados usam origem `EMAIL`.
- A deteccao de duplicidade atual verifica mesmo telefone normalizado e mesmo veiculo na mesma loja nos ultimos 7 dias.
- Duplicados sao criados com status `DUPLICATED`.
- Senhas nao sao retornadas pela API.
- A implementacao atual de criptografia e apenas Base64 para desenvolvimento.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais origens de lead e marketplaces precisam de parsers dedicados primeiro?
- E-mails importados devem ser marcados como lidos?
- Leads duplicados devem ser ignorados, mesclados ou sempre criados como `DUPLICATED`?
- A janela de duplicidade de 7 dias esta correta para todas as lojas?
- Quais informacoes precisam ser preservadas do e-mail original?

## Dashboard e Relatorios

Regras conhecidas:

- O sistema expoe metricas de dashboard para visibilidade comercial.
- Os relatorios atuais incluem status/origem de leads, performance de vendedores, periodo de vendas, indicadores de SLA e distribuicao.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais sao os KPIs oficiais do MVP?
- Quais relatorios sao obrigatorios para managers?
- Relatorios devem usar dados operacionais em tempo real ou snapshots agregados?
- Quais datas guiam metricas de conversao e tempo de resposta?
