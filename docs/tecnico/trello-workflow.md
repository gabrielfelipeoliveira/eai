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
