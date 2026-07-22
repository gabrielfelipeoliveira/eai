# Contexto Atual Do Projeto

Ultima atualizacao: 2026-07-22.

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

- Lucas Reiter: `EAI-043` em andamento no Trello.
- Gabriel Felipe Ferreira de Oliveira: nenhum card ativo conhecido no Trello.

Branches atuais:

```text
Lucas Reiter: chore/eai-043-react-router-future-flag
Gabriel Felipe Ferreira de Oliveira: sem branch ativa conhecida.
```

Proximo passo operacional:

- Finalizar `EAI-043`, abrir PR, validar CI, fazer Code Review, mergear e concluir o card no Trello.
- Depois do merge do `EAI-043`, revalidar se nao ha cards em `Em andamento`, `Aguardando Code Review` ou `Aguardando Teste`.

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
- `EAI-043`: em andamento. Remover warning React Router future flag nos E2E.

## Historico Operacional Recente

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
- Status: em andamento em 2026-07-22.
- Escopo: ativar `future.v7_startTransition` no React Router para remover warning nos E2E.
- Implementacao: `RouterProvider` recebe `future={{ v7_startTransition: true }}` e `createBrowserRouter` recebe a mesma flag via cast isolado por lacuna de tipagem da versao atual.
- Validacao: `npm run test:e2e` passou sem warning React Router future flag.
- UX: dispensado, ajuste tecnico de compatibilidade sem mudanca visual.

## Validacao Padrao

Backend:

```bash
mvn clean verify
```

`mvn verify` executa unitarios `*Test` pelo Surefire e integracoes `*IT`/`*IntegrationTest` pelo Failsafe. A suite de integracao backend usa Testcontainers e exige Docker disponivel.

Frontend:

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

Use `mvn clean verify` como validacao padrao do backend. `mvn test` sem `clean` pode reaproveitar artefatos antigos em `target/classes` e gerar falso erro de migration.

## Estado Tecnico Validado

Ultima validacao completa em 2026-07-22:

- `main` apos PR `#47`.
- GitHub Actions `CI`: Backend e Frontend passaram.
- Backend remoto: `mvn clean verify` passou.
- Frontend remoto: `npm audit --audit-level=moderate`, `npm run lint`, `npm test`, `npm run build` e `npm run test:e2e` passaram.
- Frontend local no EAI-041: `npm run build` passou sem warning de chunk acima de 500 kB.

Avisos conhecidos nao bloqueantes:

- Backend: Flyway reporta H2 2.4.240 mais novo que a versao verificada.
- Backend: Mockito usa self-attaching inline mock maker; JDK futuro pode exigir agente configurado.
- Backend: compilacao ainda pode reportar notas de uso deprecated/unchecked em alguns pontos.
- Frontend E2E: ambiente pode reportar `NO_COLOR` ignorado por `FORCE_COLOR`.
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
