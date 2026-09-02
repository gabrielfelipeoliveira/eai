# Plano De Reestruturacao UX Do Frontend

Card: `EAI-067`

Data: 2026-09-02

## Objetivo

Preparar a reestruturacao UX do frontend do EAI sem alterar regras de negocio. O foco e transformar a interface atual em uma experiencia operacional mais consistente para times comerciais automotivos, preservando os fluxos ja documentados para MVP.

## Contexto De Produto

O MVP prioriza:

- Captacao e gestao de leads.
- Pipeline comercial com atribuicao manual.
- Conversas WhatsApp por loja e dono responsavel.
- Templates, e-mail, tenancy, usuarios e LGPD basica.
- Visibilidade operacional para gestores, vendedores, pre-venda, F&I e avaliador.

Itens como SLA completo, follow-ups obrigatorios, dashboards gerenciais completos, funil configuravel, parsers dedicados e auditoria visual ficam fora do MVP ou em fase posterior.

## Diagnostico Atual

O frontend ja cobre boa parte das telas operacionais:

- `HomePage`: dashboard operacional com metricas e graficos.
- `LeadsPage`: lista de leads, filtros, metricas e drawer de criacao/detalhe.
- `PipelinePage`: pipeline por status com drag and drop manual.
- `ConversationsPage`: lista de conversas, mensagens, envio de texto, midia e template.
- `FollowUpsPage`, `OverdueLeadsPage`, `ReportsPage`: telas de apoio gerencial e segunda fase.
- `CompaniesPage`, `StoresPage`, `UsersPage`, `SettingsPage`: administracao e tenancy.
- `TemplatesPage`, `EmailAccountsPage`: canais e configuracoes operacionais.

Principais pontos de UX observados no codigo:

- Navegacao lateral permanente em desktop, sem estrategia mobile clara.
- Muitas telas usam `Paper`, `Table`, `Grid2`, `Dialog` e `Drawer` diretamente, com pouca padronizacao de cabecalho, filtros, acoes, estados vazios e erros.
- Listas operacionais densas aparecem em tabelas que podem ficar pouco ergonomicas em telas estreitas.
- Fluxos centrais de venda ficam espalhados entre Dashboard, Leads, Pipeline, Conversas e Atrasados.
- Existem telas de segunda fase visiveis no frontend (`FollowUps`, `Reports`, SLA/configuracoes), o que exige decisao de UX para diferenciar MVP ativo, apoio operacional e funcionalidade futura.
- `LeadDetailDrawer` concentra muitos subfluxos; deve virar referencia para padronizar detalhes, historico, tarefas, tags e comunicacoes.

## Principios De Reestruturacao

- Priorizar operacao diaria: achar lead, assumir, contactar, mudar status, registrar historico e responder conversa.
- Separar comando de consulta: filtros, listas e metricas devem facilitar decisao rapida, sem competir visualmente.
- Manter densidade util: SaaS operacional deve ser escaneavel, com menos elementos decorativos e mais hierarquia funcional.
- Tratar mobile como uso de consulta/acompanhamento, nao como copia espremida das tabelas desktop.
- Nao introduzir regras de negocio novas durante UX; lacunas viram pendencia de produto.

## Plano Incremental

### Fase 1: Fundacao Visual E Navegacao

- Criar shell responsivo com menu lateral desktop e navegacao adequada para mobile.
- Padronizar cabecalho de pagina, acoes primarias, breadcrumbs quando necessario e area de usuario/notificacoes.
- Definir componentes compartilhados para estados de loading, vazio, erro e sucesso.
- Padronizar densidade, espacamento, botoes de icone, tooltips e tratamento de overflow.

### Fase 2: Superficies Operacionais

- Criar padrao comum para telas de lista com filtros, tabela desktop e cards/lista mobile.
- Revisar telas de administracao para formularios e listagens consistentes.
- Reduzir duplicacao visual entre empresas, lojas, usuarios, templates e e-mails.
- Revisar tabelas com muitas colunas para priorizar informacao acionavel.

### Fase 3: Fluxos Comerciais Criticos

- Reorganizar Leads, Pipeline e Conversas como fluxo comercial continuo.
- Priorizar acoes de assumir lead, abrir detalhe, alterar status, gerar link WhatsApp e registrar contato.
- Melhorar leitura do pipeline, incluindo etapas opcionais do MVP.
- Revisar drawer de detalhe do lead para separar resumo, dados comerciais, historico, tags, tarefas e comunicacoes.

### Fase 4: Governanca De Fase

- Diferenciar visualmente o que e MVP operacional do que e segunda fase.
- Revisar Dashboard, Reports, SLA, Follow-ups e Notificacoes para evitar promessa funcional acima do escopo documentado.
- Registrar pendencias de produto quando UX depender de decisao nao documentada.

## Cards Derivados Criados

- `EAI-068` - `[Dev][Frontend] Criar shell responsivo e padroes globais de UX`: `https://trello.com/c/Znhuu0Sx`.
- `EAI-069` - `[Dev][Frontend] Padronizar listas filtros e estados responsivos`: `https://trello.com/c/JU3ratWk`.
- `EAI-070` - `[Dev][Frontend] Reestruturar fluxo Leads Pipeline Conversas`: `https://trello.com/c/Mo5QPOXr`.
- `EAI-071` - `[QA][Frontend] Validar navegacao e responsividade dos fluxos comerciais`: `https://trello.com/c/CUIQxGpx`.
- Decisao futura - tratamento visual de telas/funcionalidades de segunda fase: registrar em pendencia de produto se bloquear implementacao.

## Criterios De Aceite Para Execucao

- Cada mudanca visual deve ter card proprio e PR separada.
- Fluxos centrais devem passar por `npm run lint`, `npm test`, `npm run build` e `npm run test:e2e`.
- Alteracoes responsivas devem ser validadas em viewport desktop e mobile.
- Nenhuma tela deve introduzir regra nova sem decisao registrada em `docs/negocio/pendencias.md` ou documento oficial.
