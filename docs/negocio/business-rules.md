# Regras de Negocio

Este documento e a fonte oficial para regras de negocio definidas. Ele nao deve inventar comportamento. Quando uma regra nao estiver definida, a duvida deve ser registrada em [Pendencias de produto](pendencias.md).

## Fontes

As regras conhecidas foram extraidas da documentacao e da implementacao atuais:

- `README.md`
- `docs/tecnico/architecture.md`
- `docs/tecnico/development-guide.md`
- `docs/tecnico/email-importer.md`
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

## Atribuicao de Leads

Regras conhecidas:

- Dono manual do lead por vendedor e suportado.
- Atribuicao por manager e suportada.
- Atribuicao automatica suporta `ROUND_ROBIN` e `LEAST_BUSY`.
- Atribuicao automatica considera usuarios ativos com papel `SELLER` na loja do lead.
- `ROUND_ROBIN` atribui ao proximo vendedor ativo depois da atribuicao mais recente da loja.
- `LEAST_BUSY` atribui ao vendedor ativo com menos leads em aberto.

## SLA

Regras conhecidas:

- Politica de SLA e escopada por loja.
- Um lead esta atrasado para atribuicao quando permanece sem vendedor responsavel depois de `minutesToAssign`.
- Um lead esta atrasado para primeiro contato quando tem vendedor responsavel, nao tem `firstContactAt`, e o limite configurado passou desde a atribuicao.
- Respostas da API de lead atualmente expõem indicadores calculados de SLA.

## Follow-Ups

Regras conhecidas:

- Tarefas de follow-up sao vinculadas a um lead e a um usuario responsavel.
- Tarefas de follow-up podem ser criadas, concluidas e canceladas.
- Uma tarefa pendente vencida expoe status efetivo de atraso.
- Criar, concluir e cancelar follow-ups registra historico no lead.

## Notas, Tags e Historico

Regras conhecidas:

- Leads podem ter notas.
- Leads podem ter tags.
- Leads mantem historico de status.
- Historicos armazenam status anterior, novo status, descricao, usuario e data de criacao.
- Registros de historico criados por scheduler podem ser registros de sistema sem usuario, conforme documentacao do importador de e-mail.

## Templates de Mensagem e Links de WhatsApp

Regras conhecidas:

- Templates sao escopados por loja.
- Templates podem estar ativos ou inativos.
- Templates suportam placeholders para cliente, telefone, veiculo, vendedor, loja e cidade.
- Geracao de link de WhatsApp renderiza um template ativo da loja do lead.
- Geracao de link de WhatsApp registra uma comunicacao do lead.

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

## Dashboard e Relatorios

Regras conhecidas:

- O sistema expoe metricas de dashboard para visibilidade comercial.
- Os relatorios atuais incluem status/origem de leads, performance de vendedores, periodo de vendas, indicadores de SLA e distribuicao.
