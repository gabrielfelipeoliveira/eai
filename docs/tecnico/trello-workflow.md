# Fluxo De Trabalho No Trello

Este documento define como usar os boards do Trello para transformar decisoes de negocio em UX, desenvolvimento, testes e documentacao rastreavel.

## Boards

Boards atuais:

- `EAI - Pendencias de Negocio e Fluxo`: usado para perguntas, respostas e decisoes de produto.
- `EAI - Desenvolvimento`: usado para UX, implementacao, code review, teste e conclusao tecnica.

Nao use o board de negocio para acompanhar implementacao. Quando uma decisao exigir codigo, UX ou documentacao, crie cards derivados no board adequado e mantenha links cruzados.

## Board De Negocio

Fluxo recomendado:

1. `A responder`
2. `Respondido - atualizar docs`
3. `Documentado - gerar derivados`
4. `Aguardando validacao PO`
5. `Resolvido e documentado`
6. `Bloqueado ou revisar escopo`
7. `Cancelado ou descartado`

Significado das listas:

- `A responder`: pergunta aberta para Product Owner ou stakeholder.
- `Respondido - atualizar docs`: resposta registrada, mas ainda nao refletida na documentacao oficial.
- `Documentado - gerar derivados`: decisao ja documentada; falta criar ou linkar cards de UX, desenvolvimento, QA ou futuro.
- `Aguardando validacao PO`: existe proposta de regra, escopo ou solucao aguardando confirmacao.
- `Resolvido e documentado`: decisao registrada, documentacao atualizada e derivados criados ou dispensados explicitamente.
- `Bloqueado ou revisar escopo`: usar apenas para bloqueio real, conflito de decisao, escopo indefinido ou item que voltou para revisao.
- `Cancelado ou descartado`: item encerrado sem execucao. Exige comentario explicando motivo, responsavel pela decisao e impacto em cards relacionados.

Cards que ja tiveram decisao registrada, documentacao atualizada e tarefas derivadas criadas nao devem permanecer em `Bloqueado ou revisar escopo`.

## Board De Desenvolvimento

Fluxo recomendado:

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

Regras de uso:

- Cards que precisam definir tela, fluxo, interacao, texto de interface ou validacao visual passam por UX antes de desenvolvimento.
- Cards puramente tecnicos podem ir direto de `BACKLOG` para `Pronto para desenvolvimento`.
- UX nao deve decidir regra de negocio nova. Se surgir regra ausente durante UX, crie ou reabra card no board de negocio.
- Desenvolvimento nao deve iniciar regra de produto sem documentacao oficial ou link para decisao aprovada.
- Cards cancelados nao devem ser deletados. Mova para `Cancelado ou descartado` e registre o motivo em comentario.
- Todo novo problema, warning, vulnerabilidade, risco, melhoria ou item observado durante analise, build, teste, review ou execucao deve ser registrado no Trello antes de ser esquecido. Use comentario em card existente quando o item pertencer claramente ao escopo daquele card; caso contrario, crie um novo card no board adequado.
- Registrar um item no Trello nao obriga implementacao imediata. A decisao posterior pode ser tratar agora, manter no backlog, mover para futuro ou cancelar com justificativa.
- Sempre que um agente ou desenvolvedor mover um card, adicionar comentario operacional, abrir PR, concluir, cancelar ou alterar status, o card deve ficar atribuido ao membro responsavel pela execucao no workspace atual. Quando o trabalho for executado por IA usando o token do Lucas Reiter, atribua o card ao membro `Lucas Reiter`.

## Ligacao Entre Cards

Use a cadeia:

```text
Card de negocio -> Card UX -> Card Dev -> PR/commit -> Concluido
```

Quando um elo nao for necessario, registre isso no card anterior. Exemplo: `UX dispensado: ajuste tecnico sem impacto visual`.

Todo card derivado deve ter links de origem:

- Card `[UX]` deve apontar para o card de negocio e para a documentacao oficial.
- Card `[Dev]` deve apontar para o card de negocio, para o card UX quando existir e para a documentacao oficial.
- Card `[QA]` deve apontar para o card dev ou UX que esta validando.
- Card de negocio deve receber comentario de fechamento com os cards derivados criados ou explicitamente dispensados.

Nao remova links historicos quando o escopo mudar. Adicione um novo comentario explicando a mudanca.

## Prefixos

Use prefixos para deixar o tipo de trabalho claro:

- `[Biz]`: pergunta, decisao ou validacao de negocio.
- `[Docs]`: atualizacao documental.
- `[UX]`: fluxo, wireframe, prototipo, copy de tela ou validacao visual.
- `[Dev]`: implementacao.
- `[QA]`: validacao manual, teste exploratorio ou plano de teste.
- `[Futuro]`: item fora do MVP ou fase posterior.

## Numeracao E Branches

Cards do board `EAI - Desenvolvimento` devem ter identificador sequencial proprio do projeto no inicio do titulo:

```text
[EAI-001] [Dev][MVP] Titulo do card
```

O identificador `EAI-###` e permanente. Nao renumere cards concluidos, cancelados ou em andamento. Se um card for cancelado, mantenha o numero para preservar historico.

Use o numero do card na branch:

```text
feature/eai-001-slug-curto
bugfix/eai-001-slug-curto
hotfix/eai-001-slug-curto
chore/eai-001-slug-curto
docs/eai-001-slug-curto
test/eai-001-slug-curto
```

Tipos recomendados:

- `feature`: nova funcionalidade ou incremento funcional.
- `bugfix`: correcao em desenvolvimento normal.
- `hotfix`: correcao urgente em producao ou ambiente equivalente.
- `chore`: infraestrutura, build, CI, dependencia ou manutencao sem mudanca funcional direta.
- `docs`: documentacao.
- `test`: cobertura de testes, E2E, contratos ou qualidade automatizada.

Inclua o identificador no PR, commit principal e comentarios de fechamento quando aplicavel.

## Branches, PRs E Comentarios De Desenvolvimento

Antes de analisar ou codar um card, faca a reserva operacional do trabalho:

Esta regra nao tem excecao para mudancas pequenas, documentais ou de processo. Toda alteracao versionada deve passar por card `EAI-###`, branch, commit, push, PR, comentario no Trello e fechamento rastreavel. Nao commite direto na `main`.

1. Volte para `main`.
2. Execute `git pull --ff-only`.
3. Confirme que o workspace esta limpo.
4. Consulte o Trello e confirme que o card `EAI-###` ainda esta livre.
5. Se o card ja estiver `Em andamento`, nao inicie trabalho paralelo sem alinhamento explicito.
6. Se o card estiver livre, mova para `Em andamento`.
7. Atribua o card ao membro responsavel pela execucao no workspace atual.
8. Crie a branch seguindo o padrao deste documento.
9. Atualize `docs/tecnico/contexto-atual.md` com o card em andamento e a branch.
10. Adicione comentario de inicio no card.
11. Faca commit e push dessa reserva/handoff antes de iniciar a analise tecnica ou implementacao.

O commit inicial de reserva deve ser documental e pequeno. Exemplo:

```text
docs: EAI-006 inicia desenvolvimento
```

Modelo de comentario de inicio:

```text
## Inicio de desenvolvimento

Branch: feature/eai-###-slug-curto

Validacao inicial da main em AAAA-MM-DD:
- Backend: mvn clean verify passou com N testes, 0 falhas.
- Frontend: npm run build passou.

Status:
- Card reservado para desenvolvimento.
- Handoff atualizado em docs/tecnico/contexto-atual.md.

Proximo passo:
- Analisar documentacao e codigo relacionados antes de implementar.
```

Ao abrir PR, mova o card para `Aguardando Code Review` e adicione comentario com o link:

```text
## Pull request

PR: ...
Branch: feature/eai-###-slug-curto

Resumo:
- ...

Validacoes executadas:
- ...
```

Depois do merge e validacao final, comente o fechamento e mova o card para `Concluido`.

Modelo de comentario de conclusao:

```text
## Conclusao EAI-###

Implementado em PR: ...
Commit: ...
Branch: feature/eai-###-slug-curto

Resumo:
- ...

Documentacao atualizada:
- ...

Validacoes executadas:
- mvn clean verify com N testes, 0 falhas.
- npm run build passou.
- Smoke runtime, quando aplicavel: ...

Vulnerabilidades/avisos:
- Nenhuma nova vulnerabilidade encontrada.
- Avisos conhecidos: ...

Pendencias:
- Nenhuma, ou listar proximos cards/decisoes.
```

Commits devem manter o identificador do card no assunto ou corpo. Exemplos:

```text
feat: EAI-006 ajusta ciclo de vida e duplicidade de leads
docs: EAI-006 atualiza handoff operacional
test: EAI-016 adiciona integracao Postgres com Testcontainers
```

## Modelo De Card De Negocio

O corpo do card de negocio deve preservar a pergunta/contexto original. Respostas, decisoes, evidencias e encaminhamentos devem ser registrados em comentarios.

Formato recomendado para comentario de resolucao:

```text
## Resolucao

Decisao: ...

Criterios/regras definidos:
- ...

Impacto em documentacao: sim/nao
Impacto em UX: sim/nao
Impacto em desenvolvimento: sim/nao

Proximos cards necessarios:
- [Docs] ...
- [UX] ...
- [Dev] ...
- [QA] ...
- [Futuro] ...
```

## Modelo De Card UX

```text
Origem de negocio:
- ...

Documentacao oficial:
- ...

Objetivo:
- ...

Escopo UX:
- Fluxos:
- Telas:
- Estados:
- Permissoes visiveis:
- Mensagens/copy:

Fora de escopo:
- ...

Entregaveis:
- Link Figma/prototipo:
- Observacoes:

Criterios de aceite:
- ...

Cards derivados:
- [Dev] ...
- [QA] ...
```

## Modelo De Card Dev

```text
Origem de negocio:
- ...

Documentacao oficial:
- ...

UX:
- Card UX:
- Prototipo/Figma:

Implementacao:
- Backend:
- Frontend:
- Banco:
- Testes:
- Documentacao:

Criterios de aceite:
- ...

Riscos/pendencias:
- ...
```

## Cancelamento

Cards nao devem ser deletados como rotina. Excluir card remove contexto historico e dificulta auditoria futura.

Quando um item for cancelado, descarte ou substituido, mova o card para `Cancelado ou descartado` e registre um comentario neste formato:

```text
## Cancelamento

Motivo: ...

Decisao tomada por: ...
Data da decisao: ...

Impacto em negocio: ...
Impacto em UX: ...
Impacto em desenvolvimento: ...
Impacto em documentacao: ...

Cards relacionados:
- ...

Substituido por:
- ...
```

Se o card cancelado ja tiver cards derivados, comente tambem nos derivados ou mova os derivados para `Cancelado ou descartado`, mantendo os links cruzados.

## Regra De Fechamento

Um card de negocio so vai para `Resolvido e documentado` quando:

- a decisao estiver registrada em comentario;
- a documentacao oficial estiver atualizada ou explicitamente dispensada;
- cards derivados de UX, desenvolvimento, QA ou futuro estiverem criados e linkados, ou explicitamente dispensados;
- nao houver conflito aberto com regras existentes.

Um card de desenvolvimento so vai para `Concluido` quando:

- implementacao estiver entregue;
- validacoes relevantes tiverem sido executadas ou a ausencia delas registrada;
- documentacao tecnica/produto tiver sido atualizada quando necessario;
- vulnerabilidades apontadas por build, auditoria de dependencias, Mend/SCA ou alertas de CVE tiverem sido reportadas.
