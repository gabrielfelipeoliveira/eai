# ADR 0002: Flyway e Dono do Schema do Banco

## Status

Aceita

## Contexto

O EAI usa PostgreSQL como banco principal. O schema do banco precisa ser reproduzivel, revisavel e alinhado entre ambientes local, teste e producao.

## Decisao

Flyway e dono das migrations de schema. Mudancas de schema devem ser introduzidas por novos arquivos de migration em `backend/src/main/resources/db/migration`.

Migrations existentes nao devem ser modificadas depois de aplicadas fora de experimentacao local.

## Consequencias

- Mudancas de banco ficam explicitas e versionadas.
- Invariantes importantes podem ser reforcadas por constraints.
- Revisores conseguem inspecionar a evolucao do schema.
- Refactors que exigem mudancas de schema devem criar novas migrations.

## Decisoes Futuras

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Testes de integracao devem usar PostgreSQL/Testcontainers em vez de H2 para validar migrations com mais fidelidade?
- Um checklist formal de revisao de migrations deve ser adicionado?
