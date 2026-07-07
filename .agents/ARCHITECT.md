# Guia Do Agente Arquiteto

## Documentos Oficiais De Arquitetura

- `docs/architecture.md`
- `docs/api.md`
- `docs/database.md`
- `docs/adr/`

## Principios Centrais

- Arquitetura hexagonal.
- Ports and Adapters.
- SOLID.
- Clean Code.
- Independencia do dominio em relacao a frameworks.
- Limites explicitos entre API, aplicacao, dominio e infraestrutura.
- DTOs nao sao objetos de dominio.
- Entidades de persistencia nao sao modelos de API.

## Limites Do Backend

Dependencias permitidas:

- `api` -> `application`, `domain`
- `application` -> `domain`
- `infrastructure` -> `application`, `domain`
- `domain` -> apenas biblioteca padrao Java, salvo aprovacao explicita

Dependencias proibidas:

- `domain` -> Spring
- `domain` -> JPA
- `domain` -> DTOs da API
- `application` -> DTOs da API
- `api` -> entidades JPA

## Divida Arquitetural Atual

- Servicos de aplicacao ainda usam anotacoes Spring.
- Algumas validacoes de tenant e autorizacao estao duplicadas entre servicos.
- Alguns enriquecimentos de response sao feitos em controllers.
- A pagina de leads do frontend e grande demais e deve ser modularizada antes de grandes mudancas em leads.
- O Docker Compose atualmente cobre apenas PostgreSQL.
- O armazenamento de tokens no frontend usa local storage do navegador.
- A criptografia de senha IMAP e apenas para desenvolvimento.

## Regras De ADR

Crie ou atualize uma ADR quando uma decisao:

- Afetar limites de arquitetura.
- Alterar estrategia de persistencia.
- Alterar modelo de seguranca.
- Introduzir nova integracao externa.
- Alterar versionamento de API.
- Alterar estrategia de testes.
- Estabelecer novo padrao transversal.

Use o formato de ADR existente:

- Titulo
- Status
- Contexto
- Decisao
- Consequencias
- Decisoes futuras quando necessario

## Regras Do Agente Arquiteto

- Nao normalize conflitos entre codigo e documentacao silenciosamente.
- Registre conflitos explicitamente.
- Justifique decisoes arquiteturais relevantes.
- Prefira evolucao incremental a grandes refactors.
- Mantenha decisoes de regra de negocio com o Product Owner.
