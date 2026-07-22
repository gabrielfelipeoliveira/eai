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

## Registro Operacional EAI-012

- Status em 2026-07-21: implementado em branch `feature/eai-012-whatsapp-media-storage`.
- Escopo entregue: porta de storage de midia, adapter local/dev, metadados persistidos em `conversation_messages`, webhook com download/storage de midias recebidas, envio autenticado de midia e download autenticado.
- Validacoes: backend `rtk mvn clean verify` passou com 68 testes; frontend `rtk npm run build` passou com aviso existente de chunk JS acima de 500 kB apos minificacao. Teste HTTP local validou login, `POST /api/conversations/{id}/media` e `GET /api/conversations/{conversationId}/messages/{messageId}/media` contra backend em `8080`.

## Registro Operacional EAI-013

- Status em 2026-07-22: aguardando Code Review no PR `https://github.com/gabrielfelipeoliveira/eai/pull/19`, branch `feature/eai-013-seeds-demo-producao`.
- Escopo planejado: separar migrations de schema, seed tecnico obrigatorio e massa demonstrativa; garantir que producao execute apenas `classpath:db/migration`; atualizar testes e documentacao relacionados.
- Validacoes: backend `rtk mvn clean verify` passou com 99 testes; frontend `rtk npm run build` passou com warning conhecido de chunk JS acima de 500 kB apos minificacao.

## Registro Operacional EAI-014

- Status em 2026-07-22: implementado backend/API para fluxo LGPD basico manual por `ADMIN`.
- Escopo entregue: migration `V10__lgpd_requests.sql`, dominio/aplicacao LGPD, adapters JPA, API `/api/lgpd-requests` e testes de servico/MockMvc.
- Fora do escopo entregue: frontend/tela administrativa de LGPD, navegacao e cliente TypeScript. Criar card separado para interface administrativa consumindo `/api/lgpd-requests`.
- Regra do MVP: registrar acoes manuais nao altera automaticamente dados pessoais em leads, conversas, mensagens, usuarios ou midias.
- Validacoes: backend `rtk test mvn test` passou.

## Registro Operacional EAI-016

- Status em 2026-07-22: concluido no PR `https://github.com/gabrielfelipeoliveira/eai/pull/21`, branch `test/eai-016-testcontainers-postgres`.
- Escopo entregue: dependencias Testcontainers PostgreSQL, Maven Failsafe para testes `*IT`/`*IntegrationTest`, base abstrata de integracao com PostgreSQL 16 e smoke de Flyway, autenticacao, tenancy, leads e conversas.
- Correcao tecnica incluida: fluxo de conversas/WhatsApp passou a normalizar telefones pelo mesmo `PhoneNormalizer` de leads, preservando E.164 em PostgreSQL real.
- Validacoes: backend `rtk mvn clean verify` passou com 106 testes unitarios no Surefire e 2 testes de integracao no Failsafe/Testcontainers.

## Registro Operacional EAI-020

- Status em 2026-07-22: aguardando Code Review no PR `https://github.com/gabrielfelipeoliveira/eai/pull/22`, branch `chore/eai-020-lombok-seguro`.
- Escopo entregue: services, controllers, adapters e configuracoes com construtores triviais equivalentes passaram a usar `@RequiredArgsConstructor`; dominio manteve `@Getter`; entidades JPA mantiveram `@Getter/@Setter`; DTOs `record` preservados.
- Validacoes: backend `rtk mvn clean verify` passou com 106 testes unitarios no Surefire e 2 testes de integracao no Failsafe/Testcontainers.

Toda mudanca versionada deve seguir o fluxo de card `EAI-###`, branch, commit, push, PR, comentario no Trello e fechamento rastreavel. Isso inclui documentacao, processo e ajustes pequenos. Nao commite direto na `main`.

Todo novo problema, warning, vulnerabilidade, risco, melhoria ou item observado deve ser registrado no Trello como card novo ou comentario em card existente. Registrar no Trello nao significa tratar imediatamente; significa preservar historico para decisao posterior.

Cards em backlog, prontos ou apenas comentados/triados devem ficar sem membro por padrao. Atribua membro somente quando o card for efetivamente puxado para execucao pelo responsavel operacional. Quando a execucao for feita por IA usando o token do Lucas Reiter, atribua o card puxado ao membro `Lucas Reiter`.

## Fontes De Verdade

- Git e documentacao versionada: memoria permanente de arquitetura, regras e contexto tecnico.
- Trello de negocio: decisoes, perguntas, validacoes e historico de produto.
- Trello de desenvolvimento: execucao tecnica, UX, QA, status, links para PRs e commits.
- PRs e commits: evidencia final da implementacao.

Ferramentas de IA sao executores temporarios. Git, docs e Trello sao a memoria permanente do projeto.

## Boards Trello

- Negocio: `EAI - Pendencias de Negocio e Fluxo`
- Desenvolvimento: `EAI - Desenvolvimento`

O board de desenvolvimento usa identificadores permanentes no titulo do card:

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

## Estado Atual Do Trello

Cards de desenvolvimento conhecidos:

- `EAI-001`: concluido. Alinhar papeis: remover `AUDITOR` do MVP e incluir `AVALIADOR`.
- `EAI-002`: concluido. Ajustar tenancy: empresa agrupadora, loja operacional e desativacao sem apagar historico.
- `EAI-003`: concluido. Implementar sessao unica, TTL de 30 dias e revogacao por desativacao.
- `EAI-004`: concluido. Alinhar status de lead e pipeline com etapas opcionais de F&I.
- `EAI-005`: concluido. Modelar Item, Veiculo, telefone E.164 e moeda de venda.
- `EAI-006`: concluido. Ajustar ciclo de vida, recontato, duplicidade e telefones de lead.
- `EAI-007`: concluido. Ajustar visibilidade, ordenacao e busca normalizada de leads.
- `EAI-008`: concluido. Ajustar notas, observacoes, tags globais e historico de lead.
- `EAI-009`: concluido. Ajustar importacao de leads por e-mail, historico persistente e duplicidade por telefone+loja.
- `EAI-010`: concluido. Ajustar templates WhatsApp, placeholders, soft delete e status Meta.
- `EAI-011`: concluido. Ajustar conversas WhatsApp: dono, fila da loja e supervisao gerencial.
- `EAI-012`: implementado em 2026-07-21. Armazenamento local/dev de midias WhatsApp via porta de storage, contrato preparado para bucket equivalente, webhook com download/storage de midias recebidas, envio autenticado de midia e download autenticado.
- `EAI-013`: aguardando Code Review em 2026-07-22 no PR `#19`. Separar seeds obrigatorios de dados demonstrativos e bloquear demo em producao.
- `EAI-014`: implementado em 2026-07-22. Implementar fluxo LGPD basico manual por ADMIN.
- `EAI-015`: concluido. Padronizar validacao CI com backend clean verify e frontend build.
- `EAI-016`: concluido em 2026-07-22 no PR `#21`. Adicionar testes de integracao com Postgres via Testcontainers. UX dispensado: ajuste tecnico sem impacto visual.
- `EAI-017`: concluido em 2026-07-22 no PR `#39`. Adicionar testes unitarios e de componentes no frontend.
- `EAI-018`: concluido em 2026-07-22 no PR `#40`. Criar testes E2E dos fluxos criticos do MVP.
- `EAI-019`: concluido em 2026-07-22 no PR `#23`. Adicionar validacao de contrato OpenAPI. UX dispensado: ajuste tecnico sem impacto visual.
- `EAI-020`: concluido em 2026-07-22 no PR `#22`. Padronizar uso seguro de Lombok no backend. UX dispensado: ajuste tecnico sem impacto visual.
- `EAI-021`: concluido. Reforcar obrigatoriedade de branch e PR para qualquer mudanca.
- `EAI-022`: concluido. Registrar novos problemas no Trello e atribuir cards movimentados.
- `EAI-023`: concluido. Ajustar atribuicao de membros apenas em cards puxados.
- `EAI-024`: concluido. Notificar administradores sobre falhas de e-mail quando houver infraestrutura de notificacao.
- `EAI-025`: concluido em 2026-07-22 no PR `#27`. Atualizar Vite/esbuild por vulnerabilidades npm audit.
- `EAI-026`: concluido em 2026-07-22 no PR `#24`. Formalizar checklist de Code Review e registro de debitos por card. UX dispensado: ajuste de processo/documentacao sem impacto visual.
- `EAI-027`: concluido em 2026-07-22 no PR `#28`. Registrar comportamentos de qualidade e otimizacao do agente. UX dispensado: ajuste de processo/documentacao sem impacto visual.
- `EAI-029`: concluido em 2026-07-22 no PR `#30`. Substituir criptografia Base64 de credenciais IMAP. UX dispensado: hardening backend sem impacto visual.
- `EAI-032`: concluido em 2026-07-22 no PR `#32`. Parametrizar CORS, Swagger e defaults locais por ambiente. UX dispensado: hardening backend/config sem impacto visual.
- `EAI-034`: concluido em 2026-07-22 no PR `#31`. Atualizar GitHub Actions para remover warning de Node.js 20 deprecated. UX dispensado: ajuste de CI sem impacto visual.
- `EAI-035`: concluido em 2026-07-22 no PR `#33`. Definir rotacao e migracao de credenciais IMAP legadas. UX dispensado: hardening operacional sem impacto visual.
- `EAI-028`: concluido em 2026-07-22 no PR `#34`. Validar assinatura do webhook publico do WhatsApp. UX dispensado: hardening backend sem impacto visual.
- `EAI-031`: concluido em 2026-07-22 no PR `#35`. Limitar e validar upload/download de midias WhatsApp. UX dispensado: hardening backend sem impacto visual.
- `EAI-033`: concluido em 2026-07-22 no PR `#36`. Endurecer implementacao de JWT. UX dispensado: hardening backend sem impacto visual.
- `EAI-036`: concluido em 2026-07-22 no PR `#37`. Implementar keyring e recriptografia de credenciais IMAP. UX dispensado: hardening backend sem impacto visual.
- `EAI-037`: concluido em 2026-07-22 no PR `#42`. Corrigir lint local com artefatos Playwright ignorados.
- `EAI-030`: concluido em 2026-07-22 no PR `#38`. Reduzir exposicao de tokens no frontend e fluxo de refresh. UX dispensado: hardening de autenticacao sem mudanca visual planejada.

Antes de iniciar desenvolvimento, confirme no Trello se o status do card ainda esta atual.

## Proximo Desenvolvimento

Cards em andamento por responsavel:

- Lucas Reiter: nenhum card ativo conhecido no Trello.
- Gabriel Felipe Ferreira de Oliveira: nenhum card ativo conhecido no Trello.

Branches atuais:

```text
Lucas Reiter: sem branch ativa conhecida.
Gabriel Felipe Ferreira de Oliveira: sem branch ativa conhecida.
```

Reserva operacional EAI-017 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `test/eai-017-frontend-tests`.
- Escopo: configurar stack de testes frontend, adicionar script de teste e cobrir fluxo de login/autenticacao, autorizacao por role e uma tela critica com cobertura inicial.
- UX dispensado: ajuste de qualidade automatizada sem mudanca visual planejada.

Implementacao EAI-017 em 2026-07-22:

- Frontend passou a usar Vitest com `jsdom`, Testing Library, `@testing-library/jest-dom` e `@testing-library/user-event`.
- Scripts `npm test` e `npm run test:watch` adicionados ao frontend.
- CI passou a executar `npm test` antes de `npm run build` no job Frontend.
- Cobertura inicial adicionada para fluxo de login, autorizacao por papel em rota protegida e tela critica de lojas com listagem, status e validacao de cadastro.
- Documentacao tecnica atualizada com stack adotada e criterio de novos testes frontend com descricoes em PT-BR.
- Status operacional: concluido no PR `https://github.com/gabrielfelipeoliveira/eai/pull/39`.

Reserva operacional EAI-018 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `test/eai-018-e2e-critical-flows`.
- Escopo: configurar stack E2E e cobrir fluxos criticos do MVP no navegador, priorizando login/autenticacao, autorizacao e jornada principal de lead/listagem.
- UX dispensado: ajuste de qualidade automatizada sem mudanca visual planejada.

Implementacao EAI-018 em 2026-07-22:

- Frontend passou a usar Playwright com projeto Chromium para testes E2E.
- Scripts `npm run test:e2e` e `npm run test:e2e:ui` adicionados ao frontend.
- CI instala Chromium do Playwright e executa E2E apos testes unitarios/componentes e build frontend.
- E2E cobre redirecionamento de rota protegida sem login, login de administrador, bloqueio de tela administrativa para vendedor, listagem/filtro de leads e criacao manual de lead.
- API dos E2E e mockada nas fronteiras HTTP para manter a suite independente de backend/banco no CI.
- Status operacional: concluido no PR `https://github.com/gabrielfelipeoliveira/eai/pull/40`.

Reserva operacional EAI-037 em 2026-07-22:

- Card criado em `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `bugfix/eai-037-eslint-playwright-ignore`.
- Escopo: corrigir `npm run lint` quando artefatos ignorados do Playwright nao existem localmente.
- UX dispensado: ajuste de ferramenta sem impacto visual.

Implementacao EAI-037 em 2026-07-22:

- ESLint passou a ignorar `playwright-report` e `test-results`.
- Validado `npm run lint` apos remover o diretorio ignorado `frontend/test-results`.
- Status operacional: concluido no PR `https://github.com/gabrielfelipeoliveira/eai/pull/42`.

Reserva operacional EAI-030 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `chore/eai-030-token-exposure-hardening`.
- Escopo: avaliar e implementar reducao de exposicao de tokens no frontend, priorizando refresh token fora do JavaScript em ambiente compartilhado/producao e mantendo sessao unica/rotacao de refresh token.
- UX dispensado: hardening de autenticacao sem mudanca visual planejada.

Implementacao EAI-030 em 2026-07-22:

- Backend passou a emitir refresh token em cookie `HttpOnly`, `SameSite=Strict` e `Secure=true` por padrao em producao.
- Respostas de login/refresh retornam access token no JSON e nao expõem o valor do refresh token no corpo.
- Refresh aceita cookie `eai.refreshToken`; corpo com `refreshToken` foi mantido apenas como compatibilidade tecnica temporaria.
- Logout remove o cookie de refresh e revoga as sessoes do usuario.
- CORS passou a permitir credenciais para origens explicitamente configuradas.
- Frontend passou a manter access token apenas em memoria e a usar `withCredentials` para login, refresh, logout e chamadas autenticadas.
- `tokenStorage` remove chaves legadas `eai.accessToken` e `eai.refreshToken` do `localStorage`.
- Documentacao atualizada em ADR 0004, API e arquitetura.
- Validacao backend focada: Docker `mvn -Dtest=AuthControllerTest,AuthServiceTest test` passou com 13 testes.
- Validacao backend completa: Docker `mvn clean verify` passou com 146 testes unitarios no Surefire e 2 testes de integracao no Failsafe/Testcontainers.
- Validacoes frontend: `npm run lint` passou com 1 warning conhecido de Fast Refresh em `useAuth.tsx`; `npm run build` passou com warning conhecido de chunk acima de 500 kB.

Reserva operacional EAI-036 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `chore/eai-036-imap-keyring-reencryption`.
- Escopo: implementar keyring para credenciais IMAP com chave atual e chaves anteriores, permitindo recriptografia idempotente de credenciais legadas ou cifradas com chave anterior sem expor segredos em logs.
- UX dispensado: hardening backend sem impacto visual.

Implementacao EAI-036 em 2026-07-22:

- `EncryptionService` passou a indicar quando uma credencial precisa de recriptografia.
- AES/GCM de credenciais IMAP passou a aceitar chave atual e chaves anteriores via `EAI_EMAIL_CREDENTIALS_PREVIOUS_SECRETS`.
- Novas gravacoes seguem usando apenas `EAI_EMAIL_CREDENTIALS_SECRET`.
- Criado servico transacional e job administrativo opt-in por `EAI_EMAIL_CREDENTIALS_REENCRYPT_ON_STARTUP=true` para recriptografar credenciais legadas Base64 ou cifradas com chave anterior.
- A recriptografia e idempotente, registra apenas metricas agregadas e preserva o valor original em falha parcial.
- Validacao focada: Docker `mvn -Dtest=AesGcmEmailCredentialEncryptionServiceTest,EmailCredentialReencryptionServiceTest,EmailCredentialReencryptionJobTest test` passou com 11 testes.
- Validacao backend completa: Docker `mvn clean verify` passou com 145 testes unitarios no Surefire e 2 testes de integracao no Failsafe/Testcontainers.

Reserva operacional EAI-033 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `chore/eai-033-jwt-hardening`.
- Escopo: endurecer validacao de JWT, incluindo claims obrigatorias, algoritmo esperado, comparacao resistente a timing e qualidade minima do segredo fora de local.
- UX dispensado: hardening backend sem impacto visual.

Reserva operacional EAI-031 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `chore/eai-031-whatsapp-media-limits`.
- Escopo: definir limites parametrizados e validacoes para upload/download de midias WhatsApp no backend, cobrindo arquivo vazio, acima do limite, MIME proibido e fluxo valido.
- UX dispensado: hardening backend sem impacto visual.

Reserva operacional EAI-028 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `chore/eai-028-whatsapp-webhook-signature`.
- Escopo: validar a assinatura do payload publico do WhatsApp usando `META_WHATSAPP_APP_SECRET` e o header oficial do provedor antes de processar eventos.
- UX dispensado: hardening backend sem impacto visual.

Reserva operacional EAI-035 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `docs/eai-035-imap-credential-rotation`.
- Escopo: documentar politica operacional de rotacao do segredo IMAP e estrategia de migracao segura das credenciais legadas.
- UX dispensado: hardening operacional sem impacto visual.

Implementacao EAI-035 em 2026-07-22:

- Documentada politica operacional de rotacao de `EAI_EMAIL_CREDENTIALS_SECRET`.
- Documentada estrategia segura para migracao de credenciais IMAP legadas em Base64 e credenciais cifradas com chave anterior.
- Registrado que a implementacao atual nao deve trocar segredo diretamente em producao sem keyring, job de recriptografia ou migracao manual controlada.
- Criado card tecnico `EAI-036` para suporte a `EAI_EMAIL_CREDENTIALS_PREVIOUS_SECRETS` ou keyring equivalente e job/script idempotente de recriptografia.

Reserva operacional EAI-032 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `chore/eai-032-env-hardening`.
- Escopo: parametrizar CORS, Swagger e defaults locais por ambiente, removendo defaults inseguros de producao.
- UX dispensado: hardening backend/config sem impacto visual.

Implementacao EAI-032 em 2026-07-22:

- CORS passou a usar `eai.security.cors.allowed-origins`, `allowed-methods` e `allowed-headers`.
- Producao exige `EAI_CORS_ALLOWED_ORIGINS` sem fallback local.
- SpringDoc OpenAPI/Swagger fica desabilitado por padrao em producao via `SPRINGDOC_API_DOCS_ENABLED=false` e `SPRINGDOC_SWAGGER_UI_ENABLED=false`.
- Documentacao de API e arquitetura atualizada com comportamento por ambiente.
- Validacao focada: Docker `mvn -Dtest=FlywayProfileConfigurationTest,SecurityCorsPropertiesTest,AuthControllerTest,OpenApiContractTest test` passou com 14 testes.
- Validacao backend completa: Docker `mvn clean verify` passou com 121 testes unitarios no Surefire e 2 testes de integracao no Failsafe/Testcontainers.

Reserva operacional EAI-034 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `chore/eai-034-update-actions-node24`.
- Escopo: atualizar actions oficiais do GitHub Actions para versoes com runtime Node 24, removendo warning de Node.js 20 deprecated no CI.
- UX dispensado: ajuste tecnico de CI sem impacto visual.

Implementacao EAI-034 em 2026-07-22:

- Atualizado `actions/checkout` de `v4` para `v6`.
- Atualizado `actions/setup-java` de `v4` para `v5`.
- Atualizado `actions/setup-node` de `v4` para `v6`.
- Mantido `node-version: "20"` no build frontend para nao alterar runtime da aplicacao neste card.
- Registrada regra para evitar commits que reiniciam CI apenas por status transitorio de handoff.
- Validacao local: parse YAML de `.github/workflows/ci.yml` passou.
- CI remoto no PR `#31`: Backend e Frontend passaram sem annotation de Node.js 20 deprecated no output do run.

Reserva operacional EAI-029 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `chore/eai-029-imap-credential-encryption`.
- Escopo: substituir uso de Base64 como protecao de credenciais IMAP por criptografia reversivel adequada no backend, com migracao/compatibilidade e testes.

Implementacao EAI-029 em 2026-07-22:

- Substituida a implementacao Base64 de desenvolvimento por AES/GCM com payload versionado `v1:<iv>:<ciphertext>`.
- Mantida compatibilidade de leitura para credenciais IMAP legadas gravadas como Base64 simples.
- Adicionada configuracao `eai.email.credentials.secret` via `EAI_EMAIL_CREDENTIALS_SECRET`; producao exige a variavel sem fallback local.
- Atualizada documentacao tecnica de importador de e-mail e arquitetura para refletir o hardening.
- Validacao backend: Docker `mvn clean verify` passou com 117 testes unitarios no Surefire e 2 testes de integracao no Failsafe/Testcontainers.
- PR `https://github.com/gabrielfelipeoliveira/eai/pull/30` aprovado por Code Review operacional e CI remoto.

Reserva operacional EAI-027 em 2026-07-22:

- Card criado em `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `docs/eai-027-agent-quality-behaviors`.
- Escopo: documentar uso produtivo da espera de CI, gates de qualidade sugeridos por card, `@DisplayName` PT-BR em testes unitarios novos/alterados e handoff sem status transitorio enganoso.

Reserva operacional EAI-025 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `chore/eai-025-vite-esbuild`.
- Escopo: atualizar Vite/esbuild para corrigir vulnerabilidades reportadas por `npm audit`, mantendo o build frontend funcional.

Implementacao EAI-025 em 2026-07-22:

- Atualizado `vite` de `^5.4.10` para `^8.1.5` e `@vitejs/plugin-react` de `^4.3.3` para `^6.0.3`.
- Executado `npm audit fix` para atualizar `brace-expansion` vulneravel.
- Validacoes frontend: `rtk npm audit --audit-level=moderate` passou com 0 vulnerabilidades; `rtk npm run build` passou; `rtk npm run lint` passou com 0 erros e 1 warning conhecido em `src/hooks/useAuth.tsx`.
- Avisos conhecidos: build ainda reporta chunk JS acima de 500 kB apos minificacao; npm reporta script de instalacao pendente para `fsevents@2.3.3`.

Reserva operacional EAI-026 em 2026-07-22:

- Card criado em `Em andamento` e atribuido a `Lucas Reiter`.
- Branch `docs/eai-026-code-review-checklist`.
- Escopo: documentar checklist de Code Review, registro de debitos tecnicos/proximos cards e obrigacao de revisar handoff/Trello antes de merge/conclusao.

Historico operacional EAI-019 em 2026-07-22:

- Card movido para `Em andamento` e atribuido a `Lucas Reiter`.
- Validacao inicial frontend: `rtk npm run build` passou com warning conhecido de chunk JS acima de 500 kB apos minificacao.
- Validacao inicial backend: `rtk mvn clean verify` nao executou porque `mvn` nao esta disponivel no PATH local; alternativa via Docker exigiu permissao de acesso ao socket Docker e foi interrompida antes da execucao.

Implementacao em 2026-07-22:

- Adicionado teste automatizado `OpenApiContractTest` para validar que `/v3/api-docs` e publico, possui metadados e security scheme esperados, cobre endpoints Spring MVC em `com.eai.api`, declara responses e nao gera `operationId` duplicado.
- Atualizada documentacao de API para registrar a validacao automatizada do contrato OpenAPI.
- Validacoes: backend Docker `mvn clean verify` passou com 107 testes unitarios no Surefire e 2 testes de integracao no Failsafe/Testcontainers; frontend `rtk npm run build` passou com warning conhecido de chunk JS acima de 500 kB apos minificacao.

## Validacao Padrao

Backend:

```bash
mvn clean verify
```

`mvn verify` executa unitarios `*Test` pelo Surefire e integracoes `*IT`/`*IntegrationTest` pelo Failsafe. A suite de integracao backend usa Testcontainers e exige Docker disponivel.

Frontend:

```bash
npm run build
```

Quando usar Docker local:

```bash
docker run --rm -v "$PWD/backend:/workspace" -v eai-maven-cache:/root/.m2 -w /workspace maven:3.9-eclipse-temurin-21 mvn clean verify
docker run --rm -v "$PWD/frontend:/workspace" -w /workspace node:20-alpine npm run build
```

Use `mvn clean verify` como validacao padrao do backend. `mvn test` sem `clean` pode reaproveitar artefatos antigos em `target/classes` e gerar falso erro de migration.

## Estado Tecnico Validado

Ultima validacao em 2026-07-20:

- Branch `feature/eai-015-validacao-ci-build`.
- Backend `mvn clean verify` via Docker passou com 91 testes, 0 falhas, 0 erros e 0 skips.
- Frontend `npm run build` passou.
- Frontend `npm run lint` passou com 0 erros e 1 warning ja comentado no card `EAI-015`.
- GitHub Actions PR `#17`: checks `Backend` e `Frontend` passaram com sucesso.
- Avisos conhecidos:
- Flyway reporta H2 2.4.240 mais novo que a versao verificada.
- SpringDoc `/v3/api-docs` e `/swagger-ui.html` habilitados por default.
- Mockito usa self-attaching inline mock maker; JDK futuro pode exigir agente configurado.
- Vite reporta chunk JS acima de 500 kB apos minificacao.
- `npm audit` reporta vulnerabilidades em Vite/esbuild; registrado no card `EAI-025`.

## Lacunas Ja Registradas Em Cards

Nao crie novos cards duplicados sem antes verificar o Trello:

- Duplicidade, recontato e telefones de lead: `EAI-006` concluido.
- Visibilidade, ordenacao e busca normalizada de leads: `EAI-007`.
- Notas, observacoes, tags globais e historico de lead: `EAI-008`.
- Importacao de leads por e-mail e duplicidade por telefone+loja: `EAI-009`.
- WhatsApp templates, conversas, midias e bucket: `EAI-010` a `EAI-012`.
- Seeds obrigatorios versus dados demonstrativos: `EAI-013`.
- LGPD ADMIN manual: `EAI-014`.
- Qualidade automatizada, CI, Testcontainers, frontend tests, E2E e OpenAPI: `EAI-015` a `EAI-019`.
- Padronizacao segura de Lombok no backend: `EAI-020`.
- Vulnerabilidades npm audit em Vite/esbuild: `EAI-025`.

## Encerramento De Sessao

Ao finalizar trabalho relevante:

1. Atualize o card Trello com status, comentario e links de PR/commit quando existirem.
2. Atualize documentacao se regra, arquitetura, API, setup ou processo mudou.
3. Atualize este arquivo quando mudar o card em andamento, proximo card recomendado, validacao padrao ou algum risco relevante.
4. Reporte validacoes executadas e qualquer vulnerabilidade encontrada.
