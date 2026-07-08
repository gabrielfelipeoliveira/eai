# Prompts Uteis

Estes prompts ajudam agentes futuros a trabalhar de forma consistente.

## Prompt De Implementacao

```text
Leia `.agents/AGENTS.md`, `docs/README.md` e os arquivos relevantes em `docs/`.
Analise o codigo afetado antes de editar.
Implemente apenas o comportamento aprovado.
Nao invente regras de negocio.
Se o comportamento estiver ausente, pare e liste perguntas para o Product Owner.
Respeite a arquitetura hexagonal e adicione testes quando apropriado.
Reporte arquivos alterados e validacoes executadas.
```

## Prompt De Esclarecimento De Regra De Negocio

```text
Revise `docs/negocio/business-rules.md` e `docs/negocio/use-cases.md` para o comportamento solicitado.
Identifique regras conhecidas, conflitos e decisoes ausentes.
Nao implemente codigo.
Retorne perguntas para o Product Owner usando o formato documentado de pendencia.
```

## Prompt De Revisao De Arquitetura

```text
Revise a mudanca proposta contra `docs/tecnico/architecture.md` e `docs/tecnico/adr/`.
Verifique direcao de dependencias, responsabilidades de camadas, ports/adapters, limites de DTO, vazamento de persistencia e responsabilidades de controllers.
Liste achados por severidade com referencias de arquivo.
Sugira ADRs se novas decisoes arquiteturais forem necessarias.
```

## Prompt De Revisao De Codigo

```text
Atue como revisor usando `.agents/REVIEWER.md`.
Priorize bugs, regressoes, testes ausentes, seguranca, violacoes de arquitetura e suposicoes de regra de negocio.
Achados primeiro, depois perguntas, depois resumo breve.
```

## Prompt De Refactor

```text
Refatore apenas para objetivos aprovados de arquitetura ou manutenibilidade.
Nao altere comportamento.
Leia testes e padroes existentes primeiro.
Mantenha a mudanca pequena.
Rode testes relevantes.
Documente qualquer risco de comportamento ou cobertura ausente.
```

## Prompt De Documentacao

```text
Atualize documentacao sem alterar comportamento do sistema.
Preserve informacoes uteis existentes.
Mantenha consistencia entre README, docs, ADRs e .agents.
Quando houver informacao ausente, registre como pendencia em vez de assumir.
```

## Prompt De Testes

```text
Adicione ou melhore testes para comportamento existente e aprovado.
Nao altere comportamento de producao salvo para corrigir bug verificado.
Prefira testes focados de dominio/aplicacao para regras de negocio e testes de integracao para limites.
Rode validacoes relevantes e reporte os resultados.
```
