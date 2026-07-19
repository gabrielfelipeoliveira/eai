# Contexto Atual Do Projeto

Ultima atualizacao: 2026-07-19.

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
- `EAI-007`: backlog. Ajustar visibilidade, ordenacao e busca normalizada de leads.
- `EAI-008`: backlog. Ajustar notas, observacoes, tags globais e historico de lead.
- `EAI-009`: backlog. Ajustar importacao de leads por e-mail e duplicidade por telefone+loja.
- `EAI-010`: backlog. Ajustar templates WhatsApp, placeholders, soft delete e status Meta.
- `EAI-011`: backlog. Ajustar conversas WhatsApp: dono, fila da loja e supervisao gerencial.
- `EAI-012`: backlog. Implementar armazenamento de midias WhatsApp em bucket.
- `EAI-013`: backlog. Separar seeds obrigatorios de dados demonstrativos e bloquear demo em producao.
- `EAI-014`: backlog. Implementar fluxo LGPD basico manual por ADMIN.
- `EAI-015`: backlog. Padronizar validacao CI com backend clean verify e frontend build.
- `EAI-016`: backlog. Adicionar testes de integracao com Postgres via Testcontainers.
- `EAI-017`: backlog. Adicionar testes unitarios e de componentes no frontend.
- `EAI-018`: backlog. Criar testes E2E dos fluxos criticos do MVP.
- `EAI-019`: backlog. Adicionar validacao de contrato OpenAPI.
- `EAI-020`: backlog. Padronizar uso seguro de Lombok no backend.
- `EAI-021`: em andamento. Reforcar obrigatoriedade de branch e PR para qualquer mudanca.

Antes de iniciar desenvolvimento, confirme no Trello se o status do card ainda esta atual.

## Proximo Desenvolvimento

Card em andamento:

- `EAI-021` - Reforcar obrigatoriedade de branch e PR para qualquer mudanca.

Branch sugerida:

```text
docs/eai-021-reforca-fluxo-pr
```

## Validacao Padrao

Backend:

```bash
mvn clean verify
```

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

Ultima validacao em 2026-07-19:

- Branch `feature/eai-006-ciclo-vida-recontato-duplicidade-telefones`.
- Backend `mvn clean verify` via Docker passou com 64 testes, 0 falhas.
- Frontend `npm run build` via Docker passou.
- Avisos conhecidos:
- Flyway reporta H2 2.4.240 mais novo que a versao verificada.
- SpringDoc `/v3/api-docs` e `/swagger-ui.html` habilitados por default.
- Mockito usa self-attaching inline mock maker; JDK futuro pode exigir agente configurado.
- Vite reporta chunk JS acima de 500 kB apos minificacao.

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

## Encerramento De Sessao

Ao finalizar trabalho relevante:

1. Atualize o card Trello com status, comentario e links de PR/commit quando existirem.
2. Atualize documentacao se regra, arquitetura, API, setup ou processo mudou.
3. Atualize este arquivo quando mudar o card em andamento, proximo card recomendado, validacao padrao ou algum risco relevante.
4. Reporte validacoes executadas e qualquer vulnerabilidade encontrada.
