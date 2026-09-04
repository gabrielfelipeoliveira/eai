# Contexto Atual Do Projeto

Ultima atualizacao: 2026-09-03.

Este arquivo e o handoff operacional do projeto EAI. Ele existe para que qualquer desenvolvedor ou agente de IA consiga retomar o trabalho sem depender do historico de uma conversa especifica.

Nao registre tokens, senhas, chaves de API ou dados sensiveis neste arquivo.

## Como Comecar Uma Sessao

Antes de implementar qualquer mudanca, leia:

1. `AGENTS.md`
2. `.agents/AGENTS.md`
3. `docs/README.md`
4. `docs/tecnico/contexto-atual.md`
5. `docs/tecnico/trello-workflow.md`
6. Documentos de negocio e tecnica relacionados ao card atual
7. Card `EAI-###` correspondente no Trello

Se documentacao, Trello e codigo estiverem em conflito, nao escolha um lado silenciosamente. Registre o conflito no card ou na documentacao apropriada antes de implementar.

## Regras Operacionais

- Toda mudanca versionada deve seguir o fluxo de card `EAI-###`, branch, commit, push, PR, comentario no Trello e fechamento rastreavel.
- Nao commite direto na `main`.
- Use Trello como lock operacional entre devs e agentes.
- Cards em backlog, prontos ou apenas triados ficam sem membro por padrao.
- Atribua membro somente quando o card for efetivamente puxado para execucao.
- Quando a execucao for feita por IA usando o token do Lucas Reiter, atribua o card puxado ao membro `Lucas Reiter`.
- Todo novo problema, warning, vulnerabilidade, risco, melhoria ou item observado deve ser registrado no Trello como card novo ou comentario em card existente.
- Registrar no Trello nao significa tratar imediatamente; significa preservar historico para decisao posterior.
- Em conflito por paralelismo, a branch que ainda falta mergear deve revisar se, apos resolver conflito, ambos os contextos operacionais continuam atualizados.

## Fontes De Verdade

- Git e documentacao versionada: memoria permanente de arquitetura, regras e contexto tecnico.
- Trello de negocio: decisoes, perguntas, validacoes e historico de produto.
- Trello de desenvolvimento: execucao tecnica, UX, QA, status, links para PRs e commits.
- PRs e commits: evidencia final da implementacao.

Ferramentas de IA sao executores temporarios. Git, docs e Trello sao a memoria permanente do projeto.

## Boards Trello

- Negocio: `EAI - Pendencias de Negocio e Fluxo`
- Desenvolvimento: `EAI - Desenvolvimento`

Fluxo principal do board de desenvolvimento:

1. `BACKLOG`
2. `Pronto para UX`
3. `UX em andamento`
4. `UX validado`
5. `Pronto para desenvolvimento`
6. `Em andamento`
7. `Aguardando Code Review`
8. `Aguardando Teste`
9. `Concluido`
10. `Cancelado ou descartado`

Use o identificador permanente no titulo do card:

```text
[EAI-003] [Dev][MVP] Titulo do card
```

Use o identificador na branch:

```text
feature/eai-003-slug-curto
bugfix/eai-003-slug-curto
hotfix/eai-003-slug-curto
chore/eai-003-slug-curto
docs/eai-003-slug-curto
test/eai-003-slug-curto
```

## Estado Atual

Cards em andamento por responsavel:

- Lucas Reiter: `EAI-065` em andamento para revalidar excecoes Trivy do `gosu` em `postgres:16-bookworm`.
- Gabriel Felipe Ferreira de Oliveira: nenhum card ativo conhecido no Trello.

Branches atuais:

```text
Lucas Reiter: `feature/eai-065-revisa-trivy-gosu`.
Gabriel Felipe Ferreira de Oliveira: sem branch ativa conhecida.
```

Proximo passo operacional:

- Finalizar `EAI-065`: validar CI, abrir PR para `main`, revisar e fechar Trello.
- Proximos itens de negocio seguem no quadro `EAI - Pendencias de Negocio e Fluxo`, lista `A responder`.

## Cards De Desenvolvimento Conhecidos

Todos os cards abaixo ficam no board `EAI - Desenvolvimento`. Consulte sempre o Trello antes de iniciar qualquer um.

- `EAI-001`: concluido. Alinhar papeis: remover `AUDITOR` do MVP e incluir `AVALIADOR`.
- `EAI-002`: concluido. Ajustar tenancy: empresa agrupadora, loja operacional e desativacao sem apagar historico.
- `EAI-003`: concluido. Implementar sessao unica, TTL de 30 dias e revogacao por desativacao.
- `EAI-004`: concluido. Alinhar status de lead e pipeline com etapas opcionais de F&I.
- `EAI-005`: concluido. Modelar Item, Veiculo, telefone E.164 e moeda de venda.
- `EAI-006`: concluido. Ajustar ciclo de vida, recontato, duplicidade e telefones de lead.
- `EAI-007`: concluido. Ajustar visibilidade, ordenacao e busca normalizada de leads.
- `EAI-008`: concluido. Ajustar notas, observacoes, tags globais e historico de lead.
- `EAI-009`: concluido. Ajustar importacao de leads por e-mail e duplicidade por telefone+loja.
- `EAI-010`: concluido. Ajustar templates WhatsApp, placeholders, soft delete e status Meta.
- `EAI-011`: concluido. Ajustar conversas WhatsApp: dono, fila da loja e supervisao gerencial.
- `EAI-012`: concluido. Implementar armazenamento de midias WhatsApp em bucket/adapter equivalente.
- `EAI-013`: concluido. Separar seeds obrigatorios de dados demonstrativos e bloquear demo em producao.
- `EAI-014`: concluido. Implementar fluxo LGPD basico manual por `ADMIN`.
- `EAI-015`: concluido. Padronizar validacao CI com backend `clean verify` e frontend build.
- `EAI-016`: concluido. Adicionar testes de integracao com Postgres via Testcontainers.
- `EAI-017`: concluido no PR `#39`. Adicionar testes unitarios/componentes no frontend.
- `EAI-018`: concluido no PR `#40`. Criar testes E2E dos fluxos criticos do MVP.
- `EAI-019`: concluido. Adicionar validacao de contrato OpenAPI.
- `EAI-020`: concluido. Padronizar uso seguro de Lombok no backend.
- `EAI-021`: concluido. Reforcar obrigatoriedade de branch e PR para qualquer mudanca.
- `EAI-022`: concluido. Registrar novos problemas no Trello e atribuir cards movimentados.
- `EAI-023`: concluido. Ajustar atribuicao de membros apenas em cards puxados.
- `EAI-024`: concluido. Notificar administradores sobre falhas de e-mail quando houver infraestrutura de notificacao.
- `EAI-025`: concluido no PR `#27`. Atualizar Vite/esbuild por vulnerabilidades `npm audit`.
- `EAI-026`: concluido no PR `#24`. Formalizar checklist de Code Review e registro de debitos por card.
- `EAI-027`: concluido no PR `#28`. Registrar comportamentos de qualidade e otimizacao do agente.
- `EAI-028`: concluido no PR `#34`. Validar assinatura do webhook publico do WhatsApp.
- `EAI-029`: concluido no PR `#30`. Substituir criptografia Base64 de credenciais IMAP.
- `EAI-030`: concluido no PR `#38`. Reduzir exposicao de tokens no frontend.
- `EAI-031`: concluido no PR `#35`. Limitar e validar upload/download de midias WhatsApp.
- `EAI-032`: concluido no PR `#32`. Parametrizar CORS, Swagger e defaults locais por ambiente.
- `EAI-033`: concluido no PR `#36`. Endurecer implementacao de JWT.
- `EAI-034`: concluido no PR `#31`. Atualizar actions por warning de Node.js 20 deprecated.
- `EAI-035`: concluido no PR `#33`. Definir rotacao e migracao de credenciais IMAP legadas.
- `EAI-036`: concluido no PR `#37`. Implementar keyring e recriptografia de credenciais IMAP.
- `EAI-037`: concluido no PR `#42`. Corrigir lint local com artefatos Playwright ignorados.
- `EAI-038`: concluido no PR `#44`. Adicionar `npm audit --audit-level=moderate` e `npm run lint` no CI frontend.
- `EAI-039`: concluido no PR `#45`. Separar `AuthProvider` e `useAuth` para remover warning Fast Refresh.
- `EAI-040`: concluido no PR `#46`. Corrigir selects MUI indefinidos nos E2E de leads.
- `EAI-041`: concluido no PR `#47`. Reduzir chunk principal do build frontend.
- `EAI-042`: concluido no PR `#48`. Limpar historico operacional truncado do contexto atual.
- `EAI-043`: concluido no PR `#49`. Remover warning React Router future flag nos E2E.
- `EAI-044`: concluido no PR `#51`. Remover warning ambiental `NO_COLOR`/`FORCE_COLOR` dos E2E.
- `EAI-045`: concluido no PR `#55`. Atualizar dependencias frontend por vulnerabilidades `npm audit`.
- `EAI-046`: concluido no PR `#56`. Fechar contexto operacional do `EAI-045`.
- `EAI-047`: concluido no PR `#58`. Ampliar cobertura unitária de fluxos críticos.
- `EAI-048`: concluido no PR `#59`. Fechar contexto operacional do `EAI-047`.
- `EAI-049`: concluido no PR `#60`. Adicionar SCA Maven e coverage backend.
- `EAI-050`: concluido no PR `#62`. Zerar warnings backend e validar vulnerabilidades.
- `EAI-051`: concluido no PR `#64`. Aumentar coverage backend por ratchet.
- `EAI-052`: concluido no PR `#66`. Sequenciar testes unitarios rumo a 90% coverage.
- `EAI-053`: concluido no PR `#68`. Elevar coverage backend para 90%.
- `EAI-054`: concluido no PR `#70`. Corrigir vulnerabilidades `npm audit` frontend e SCA backend.
- `EAI-055`: concluido no PR `#71`. Definir gate Mend/Dependabot/CodeQL.
- `EAI-056`: concluido no PR `#85`. Configurar `NVD_API_KEY` para Dependency-Check.
- `EAI-057`: concluido no PR `#86`. Adicionar auditoria de imagem Docker.
- `EAI-058`: concluido no PR `#87`. Triar PRs Dependabot abertas.
- `EAI-059`: concluido. Corrigir alertas CodeQL high de CSRF e path injection.
- `EAI-060`: concluido no PR `#101`. Triar nova rodada de PRs Dependabot.
- `EAI-061`: concluido no PR `#102`. Tratar vulnerabilidades Trivy da imagem Postgres.
- `EAI-062`: concluido no PR `#100`. Atualizar handoff operacional pos `EAI-059`.
- `EAI-063`: concluido no PR `#110`. Avaliar migracao de majors e pins de dependencias frontend.
- `EAI-064`: concluido no PR `#111`. Revisar excecoes Trivy do `gosu` na imagem Postgres e renovar controle temporario ate `2026-10-31`.
- `EAI-065`: em andamento. Revalidado em 2026-09-03: excecoes Trivy do `gosu` ainda necessarias em `postgres:16-bookworm` no digest `sha256:bb3e1a57e5407e0a5280b4211980a5e537f4abd234a87014ac979849a78dd825`; revisar novamente ate `2026-10-31`.
- `EAI-066`: concluido. Triar PRs Dependabot restantes `#103` e `#104`.
- `EAI-067`: concluido no PR `#113`. Mapear reestruturacao UX do frontend.
- `EAI-068`: concluido no PR `#114`. Criar shell responsivo e padroes globais de UX.
- `EAI-069`: concluido no PR `#115`. Padronizar listas, filtros e estados responsivos.
- `EAI-070`: em andamento. Reestruturar fluxo Leads/Pipeline/Conversas.
- `EAI-071`: cancelado como duplicado/absorvido pelo `EAI-081`. Validacao de navegacao e responsividade dos fluxos comerciais coberta na validacao integrada.

## Historico Operacional Recente

### EAI-070

- Branch: `feature/eai-070-commercial-flow-ux`.
- Card: `https://trello.com/c/Znhuu0Sx`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/116`.
- Escopo: reestruturar o fluxo comercial Leads/Pipeline/Conversas conforme `docs/ux/frontend-restructuring-plan.md`.
- Entrega: `CommercialFlowNavigation` criado para navegacao contextual entre Leads, Pipeline e Conversas; `LeadsPage`, `PipelinePage` e `ConversationsPage` conectadas ao fluxo comercial com layouts responsivos e estados de loading/erro/vazio.
- Vulnerabilidade tratada: check Backend da PR `#116` falhou no OSV por vulnerabilidades criticas em `org.apache.tomcat.embed:tomcat-embed-core`; backend passou a fixar `tomcat.version` em `11.0.25`.
- Validacao local: `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build`, `npm run test:e2e`, `git diff --check`, `mvn clean verify` via Docker/Testcontainers e OSV sobre `backend/target/bom.xml` passaram.
- Status: aguardando nova rodada dos checks remotos da PR `#116` apos push da correcao de Tomcat.

### EAI-069

- Branch: `feature/eai-069-responsive-lists-states`.
- Card: `https://trello.com/c/Mo5QPOXr`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/115`.
- Escopo: padronizar listas, filtros e estados responsivos conforme `docs/ux/frontend-restructuring-plan.md`.
- Entrega: criados componentes compartilhados `PageHeader`, `ResponsiveDataView` e `RecordCard`; `CompaniesPage`, `StoresPage` e `UsersPage` passaram a alternar tabela desktop e cards mobile com estados loading/erro/vazio padronizados; `LeadsPage` passou a usar cabecalho comum, filtros com espacamento responsivo e tabela com overflow horizontal controlado.
- Validacao local: `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build`, `npm run test:e2e` e `git diff --check` passaram.
- Validacao remota: checks Backend, Frontend, Container Images e CodeQL passaram no PR `#115`; merge/squash em `7646c833c7a23b208947798e107609549a852d9e`.

### EAI-068

- Branch: `feature/eai-068-responsive-shell-ux`.
- Card: `https://trello.com/c/JU3ratWk`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/114`.
- Escopo: implementar shell responsivo e padroes globais de UX conforme `docs/ux/frontend-restructuring-plan.md`.
- Entrega: `MainLayout` ajustado com drawer permanente em desktop, drawer temporario no mobile, botao de menu, conteudo com espacamento responsivo e fechamento da navegacao ao selecionar item; adicionados testes unitario e E2E do shell responsivo.
- Validacao local: `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build`, `npm run test:e2e -- responsive-shell.spec.ts`, `npm run test:e2e` e `git diff --check` passaram.
- Validacao remota: checks Backend, Frontend, Container Images e CodeQL passaram no PR `#114`; merge/squash em `7ae2fae721d4bbf6b2292ddcb6512ee78fcc02a4`.

### EAI-067

- Branch: `docs/eai-067-frontend-ux-restructuring`.
- Card: `https://trello.com/c/BNgWJd2z`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/113`.
- Escopo: mapear a reestruturacao UX do frontend, revisar experiencia atual e gerar plano/cards derivados sem inventar regra de negocio nova.
- Entrega: documento `docs/ux/frontend-restructuring-plan.md` criado com diagnostico, fases e criterios; cards derivados `EAI-068`, `EAI-069`, `EAI-070` e `EAI-071` criados no Backlog.

### EAI-059

- Branch: `chore/eai-059-codeql-alerts`.
- Entrega: corrigidos alertas CodeQL high de CSRF e path injection.
- Commit na `main`: `6440a96 fix: EAI-059 corrige alertas CodeQL high`.
- Status Trello: concluido em `https://trello.com/c/m0MFcF77`.
- Observacao: este handoff estava atrasado e ainda apontava `EAI-059` como em andamento; correcao documental tratada no `EAI-062`.

### EAI-062

- Branch: `docs/eai-062-atualiza-handoff-pos-eai-059`.
- Card: `https://trello.com/c/uXI0qeF5`.
- Escopo: atualizar este handoff operacional para refletir a conclusao do `EAI-059` e registrar proximos cards `EAI-060` e `EAI-061`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/100`.
- Commit na `main`: `91e1b442959b1ed57af98ee64126a0e20d81e391`.
- Status Trello: concluido.
- Validacao: `git diff --check` e checks remotos Backend, Frontend, Container Images e CodeQL passaram.

### EAI-060

- Branch: `chore/eai-060-dependabot-triage`.
- Card: `https://trello.com/c/R9nEILpj`.
- Escopo: triagem da nova rodada de PRs Dependabot, com merge/correcao/cancelamento conforme checks, conflitos e vulnerabilidades.
- Entrega: PRs Dependabot backend `#90`, `#91`, `#92`, `#98` e `#99` mergeados diretamente apos checks verdes; updates frontend restantes absorvidos neste card junto com ajuste de Node 22.22.2+.
- Validacao local: `npm ci`, `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build`, `npm run setup:e2e`, `npm run test:e2e`, `mvn clean verify` via Docker/Testcontainers e OSV frontend/backend passaram.
- Achados: `jsdom 30` exige Node mais novo que o Node 20 do CI; CI, README e `frontend/package.json` foram alinhados para Node 22.22.2+. `npm outdated` ainda lista majors/pins fora do escopo seguro deste card, como MUI 9, Zod 4, TypeScript 7 e React Router 8.3.1.

### EAI-066

- Branch: `chore/eai-066-dependabot-restantes`.
- Card: `https://trello.com/c/Owt8gwX2`.
- Escopo: triagem das PRs Dependabot restantes `#103` (`actions/setup-java` 5 -> 6) e `#104` (`tools.jackson:jackson-bom` 3.1.5 -> 3.2.2).
- Entrega: PRs Dependabot `#103` e `#104` revisadas e mergeadas.
- Validacao: checks remotos Backend, Frontend, Container Images e CodeQL passaram nas duas PRs; lista de PRs abertas ficou vazia.

### EAI-064

- Branch: `chore/eai-064-postgres-gosu-trivy`.
- Card: `https://trello.com/c/F6mxDThh`.
- Escopo: revisar se as excecoes Trivy do `gosu` na imagem `postgres:16-bookworm` ainda sao necessarias antes de `2026-09-30`.
- Entrega em andamento: imagem `postgres:16-bookworm` atualizada confirmada no digest `sha256:bb3e1a57e5407e0a5280b4211980a5e537f4abd234a87014ac979849a78dd825`; Trivy `0.74.0` sem ignore ainda reporta 22 achados HIGH/CRITICAL em `usr/local/bin/gosu` e 0 achados Debian; excecoes renovadas ate `2026-10-31`.
- Validacao local: Trivy `0.74.0` com `.trivyignore.yaml` atualizado passou com 0 HIGH/CRITICAL nao ignorados em `postgres:16-bookworm`.
- Debito registrado: `EAI-065` criado para nova revisao antes de `2026-10-31` (`https://trello.com/c/2xYml9AH`).

### EAI-063

- Branch: `chore/eai-063-frontend-dependencies`.
- Card: `https://trello.com/c/S9ffu7Ej`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/110`.
- Escopo: avaliar PRs Dependabot abertos do frontend e decidir quais updates podem ser integrados agora sem bloquear a reestruturacao UX.
- Entrega: absorvidos updates frontend dos PRs Dependabot `#105`, `#106`, `#107`, `#108` e `#109` em uma branch unica; Zod 4 exige `@hookform/resolvers` 5 e tipagem explicita `z.input`/`z.output` para formularios com `z.coerce.number()`.
- Validacao local/remota: `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build`, `npm run test:e2e`, `npm ci` com Node `22.22.2` em container e checks remotos passaram.
- Achados/debitos: MUI `9.4.0` quebra APIs usadas no frontend e TypeScript `7.0.2` e rejeitado por `typescript-eslint` `8.69.0`; ambos ficam para cards futuros.

### EAI-061

- Branch: `chore/eai-061-postgres-trivy`.
- Card: `https://trello.com/c/FcEp0Cl7`.
- Escopo: investigar e tratar vulnerabilidades HIGH/CRITICAL reportadas pelo Trivy na imagem Postgres.
- Entrega: imagem local/Testcontainers/CI ajustada para `postgres:16-bookworm`; gate Trivy do CI voltou a ser bloqueante com excecoes temporarias e estreitas para `usr/local/bin/gosu` em `.trivyignore.yaml`.
- Validacao local: Trivy `0.74.0` em `postgres:16-bookworm` passou com 0 HIGH/CRITICAL nao ignorados; backend `mvn clean verify` via Docker/Testcontainers passou com 340 unitarios, 2 integracoes e PostgreSQL 16.15.
- Achado/debito: a imagem oficial ainda inclui `gosu` compilado com Go vulneravel segundo Trivy. Excecoes expiram em `2026-09-30` e devem ser removidas quando a imagem oficial atualizar o binario.

### EAI-038

- Branch: `chore/eai-038-ci-frontend-lint-audit`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/44`.
- Entrega: CI frontend passou a executar `npm audit --audit-level=moderate` e `npm run lint`.
- Validacao: CI remoto da `main` passou com Backend e Frontend verdes.
- UX: dispensado, ajuste de qualidade automatizada sem impacto visual.

### EAI-039

- Branch: `chore/eai-039-auth-fast-refresh`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/45`.
- Entrega: `AuthContext.ts` centraliza contexto/tipo; `AuthProvider.tsx` concentra o provider; `useAuth.tsx` exporta somente o hook.
- Validacao: `npm run lint` passou sem warnings; CI remoto da `main` passou.
- UX: dispensado, refatoracao interna sem impacto visual.

### EAI-040

- Branch: `bugfix/eai-040-leads-select-defaults`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/46`.
- Entrega: formulario de lead passou a definir `defaultValue` explicito nos selects `source`, `companyId` e `storeId`; empresa/loja ganharam opcao vazia desabilitada.
- Validacao: E2E passou sem warnings MUI de valores `undefined`; CI remoto da `main` passou.
- UX: dispensado, correcao de estado controlado sem mudanca planejada de layout.

### EAI-041

- Branch: `chore/eai-041-frontend-code-splitting`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/47`.
- Entrega: lazy loading por rota e `manualChunks` no Vite/Rolldown para separar React, MUI, forms, query, charts e vendor geral.
- Validacao: `npm run build` passou sem warning de chunk acima de 500 kB; CI remoto da `main` passou.
- UX: validado indiretamente por E2E, sem novo fluxo.

### EAI-042

- Branch: `docs/eai-042-clean-current-context`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/48`.
- Entrega: blocos historicos truncados/pouco legiveis substituidos por estado operacional normalizado.
- Validacao: CI remoto da `main` passou.
- UX: dispensado, ajuste documental.

### EAI-043

- Branch: `chore/eai-043-react-router-future-flag`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/49`.
- Escopo: ativar `future.v7_startTransition` no React Router para remover warning nos E2E.
- Implementacao: `RouterProvider` recebe `future={{ v7_startTransition: true }}` e `createBrowserRouter` recebe a mesma flag via cast isolado por lacuna de tipagem da versao atual.
- Validacao: `npm run test:e2e` passou sem warning React Router future flag; CI remoto da `main` passou.
- UX: dispensado, ajuste tecnico de compatibilidade sem mudanca visual.

### EAI-044

- Branch: `test/eai-044-remove-no-color-warning`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/51`.
- Entrega: script `test:e2e` remove `NO_COLOR` do ambiente antes de executar Playwright, evitando conflito com `FORCE_COLOR` em subprocessos Vite/Playwright.
- Validacao: `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build` e `npm run test:e2e` passaram; E2E local sem warning `NO_COLOR`/`FORCE_COLOR`; CI remoto do PR passou.
- UX: dispensado, ajuste tecnico de ambiente/teste sem mudanca visual.

### EAI-047

- Branch: `test/eai-047-critical-unit-coverage`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/58`.
- Entrega: cobertura unitária adicionada para helpers de exibicao de conversas no frontend e para `LeadDistributionService` no backend.
- Validacao: frontend `npm run lint`, `npm test`, `npm run build`, `npm audit --audit-level=moderate` e `npm run test:e2e` passaram; backend `mvn clean verify` passou via Docker/Testcontainers.
- Vulnerabilidades: `npm audit --audit-level=moderate` reportou 0 vulnerabilidades; backend nao possui gate SCA/Mend/OWASP Maven configurado.
- Gates futuros: avaliar card para SCA de dependencias Maven e politica minima de coverage.

### EAI-052

- Branch: `test/eai-052-coverage-backend-ratchet-2`.
- Entrega: adicionados testes unitarios para `WhatsAppCloudTemplateClient`, `FollowUpTaskService` e cenarios complementares de `LeadDistributionService`; construtor do cliente WhatsApp passou a aceitar `graphBaseUrl` injetavel para testes sem rede externa.
- Coverage: backend subiu de 76,18% para 80,10% instruction coverage; gate JaCoCo elevado de 76% para 80%. Para 90%, ainda faltam aproximadamente 2.765 instrucoes cobertas.
- Validacao backend: `mvn clean verify` via Docker/Testcontainers passou com 220 unitarios e 2 integracoes.
- Validacao frontend: `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build` e `npm run test:e2e` passaram.
- SCA: CycloneDX SBOM XML gerado; OSV Scanner reportou `No issues found`; OWASP Dependency-Check Maven ficou bloqueado por rate limit externo da NVD (`429`) sem `NVD_API_KEY`, antes de concluir a analise.

### EAI-053

- Branch: `test/eai-053-coverage-backend-90`.
- PR: `https://github.com/gabrielfelipeoliveira/eai/pull/68`.
- Entrega: adicionados testes unitarios e de contrato para controllers, services, dominio, adapters de persistencia e integracoes de email/configuracao; gate JaCoCo de instruction coverage elevado de 80% para 90%.
- Coverage: backend atingiu 90,01% instruction coverage e 90,58% line coverage.
- Validacao backend: `mvn clean verify` via Docker/Testcontainers passou com 339 unitarios, 2 integracoes e gate JaCoCo de 90%.
- Validacao frontend: `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build` e `npm run test:e2e` passaram.
- SCA: CycloneDX SBOM XML gerado; OSV Scanner reportou `No issues found`; OWASP Dependency-Check Maven ficou bloqueado por rate limit externo da NVD (`429`) sem `NVD_API_KEY`, antes de concluir a analise.

## Validacao Padrao

Backend:

```bash
mvn clean verify
mvn org.owasp:dependency-check-maven:check
```

`mvn verify` executa unitarios `*Test` pelo Surefire, integracoes `*IT`/`*IntegrationTest` pelo Failsafe e gate de coverage JaCoCo com minimo global de 90% de instrucoes. A suite de integracao backend usa Testcontainers e exige Docker disponivel. O Dependency-Check executa SCA das dependencias Maven e falha para CVSS 7.0 ou superior quando a base NVD esta acessivel.

No CI, o OWASP Dependency-Check Maven roda somente quando o segredo `NVD_API_KEY` estiver configurado no repositorio GitHub. Quando o segredo nao existe, o workflow emite um notice explicito e mantem o gate OSV sobre o SBOM backend. Para ativar o gate completo, configure o segredo `NVD_API_KEY` em Settings > Secrets and variables > Actions, ou via GitHub CLI usando uma variavel de ambiente local segura: `gh secret set NVD_API_KEY --body "$NVD_API_KEY"`. Nao registre a chave em commits, docs, logs ou conversas.

Imagens Docker:

```bash
trivy image --scanners vuln --severity HIGH,CRITICAL --ignore-unfixed --ignorefile .trivyignore.yaml --exit-code 1 postgres:16-bookworm
```

O CI executa Trivy sobre `postgres:16-bookworm` como gate bloqueante. A tag Debian Bookworm nao reporta vulnerabilidades HIGH/CRITICAL de pacotes do SO na validacao de `2026-08-30`; os achados remanescentes sao restritos ao binario `usr/local/bin/gosu` da imagem oficial e ficam documentados com expiracao em `.trivyignore.yaml`. Todo novo achado nao ignorado deve quebrar o CI e ser registrado no Trello.

Frontend:

Nota `EAI-049`: o CI gera SBOM Maven com CycloneDX e executa OSV como gate SCA sempre ativo. O OWASP Dependency-Check Maven fica configurado e roda no CI quando `NVD_API_KEY` estiver disponivel, evitando rate limit da NVD. Vulnerabilidades OSV encontradas em `jackson-databind` 2.x/3.x e `org.postgresql:postgresql` foram tratadas por BOMs/override no `pom.xml`. Coverage minimo inicial ficou em 70%; `EAI-051` elevou o ratchet para 76%; `EAI-052` elevou para 80%; `EAI-053` elevou o gate para 90% com coverage consolidado de 90,01% instruction.

```bash
npm audit --audit-level=moderate
npm run lint
npm test
npm run build
npm run test:e2e
```

Quando usar Docker local:

```bash
docker run --rm -v "$PWD/backend:/workspace" -v eai-maven-cache:/root/.m2 -w /workspace maven:3.9-eclipse-temurin-21 mvn clean verify
docker run --rm -v "$PWD/frontend:/workspace" -w /workspace node:20-alpine npm run build
```

Use `mvn clean verify` como validacao padrao do backend e rode `mvn org.owasp:dependency-check-maven:check` para SCA Maven. `mvn test` sem `clean` pode reaproveitar artefatos antigos em `target/classes` e gerar falso erro de migration.

## Estado Tecnico Validado

Ultima validacao completa em 2026-07-26:

- `main` apos PR `#68`.
- GitHub Actions `CI`: Backend e Frontend passaram.
- Backend local: `mvn clean verify` passou via Docker/Testcontainers com 339 unitarios, 2 integracoes e gate JaCoCo de 90%.
- Frontend local: `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build` e `npm run test:e2e` passaram.
- Vulnerabilidades: `npm audit --audit-level=moderate` reportou 0 vulnerabilidades no frontend; OSV Scanner sobre SBOM Maven reportou `No issues found`; OWASP Dependency-Check Maven ficou bloqueado por rate limit externo da NVD (`429`) sem `NVD_API_KEY`.

Avisos conhecidos nao bloqueantes:

- Backend: Flyway reporta H2 2.4.240 mais novo que a versao verificada.
- Backend: Mockito usa self-attaching inline mock maker; JDK futuro pode exigir agente configurado.
- Backend: compilacao ainda pode reportar notas de uso deprecated/unchecked em alguns pontos.
- Frontend E2E: warning React Router future flag `v7_startTransition` removido no `EAI-043`.

## Lacunas Ja Registradas Em Cards

Nao crie novos cards duplicados sem antes verificar o Trello:

- Duplicidade, recontato e telefones de lead: `EAI-006` concluido.
- Visibilidade, ordenacao e busca normalizada de leads: `EAI-007` concluido.
- Notas, observacoes, tags globais e historico de lead: `EAI-008` concluido.
- Importacao de leads por e-mail e duplicidade por telefone+loja: `EAI-009` concluido.
- WhatsApp templates, conversas, midias e bucket: `EAI-010` a `EAI-012` concluidos.
- Seeds obrigatorios versus dados demonstrativos: `EAI-013` concluido.
- LGPD ADMIN manual: `EAI-014` concluido.
- Qualidade automatizada, CI, Testcontainers, frontend tests, E2E e OpenAPI: `EAI-015` a `EAI-019` concluidos.
- Padronizacao segura de Lombok no backend: `EAI-020` concluido.
- Vulnerabilidades npm audit em Vite/esbuild: `EAI-025` concluido.
- Warning Fast Refresh do `useAuth`: `EAI-039` concluido.
- Warnings MUI de selects de leads em E2E: `EAI-040` concluido.
- Warning de chunk grande no build frontend: `EAI-041` concluido.

## Encerramento De Sessao

Ao finalizar trabalho relevante:

1. Atualize o card Trello com status, comentario e links de PR/commit quando existirem.
2. Atualize documentacao se regra, arquitetura, API, setup ou processo mudou.
3. Atualize este arquivo quando mudar o card em andamento, proximo card recomendado, validacao padrao ou algum risco relevante.
4. Reporte validacoes executadas e qualquer vulnerabilidade encontrada.
