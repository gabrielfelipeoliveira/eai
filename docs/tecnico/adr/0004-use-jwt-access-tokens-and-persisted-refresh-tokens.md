# ADR 0004: Usar Access Tokens JWT e Refresh Tokens Persistidos

## Status

Aceita

## Contexto

O EAI precisa de acesso autenticado a API para usuarios no navegador. A implementacao atual usa access tokens stateless e refresh tokens persistidos.

## Decisao

Usar access tokens JWT para chamadas autenticadas da API e persistir refresh tokens para permitir renovacao de sessao e revogacao logica.

## Consequencias

- Chamadas de API podem ser autenticadas sem sessoes server-side para access tokens.
- Persistencia de refresh tokens suporta logout e comportamento de revogacao.
- Hardening de seguranca deve tratar armazenamento de tokens, tempo de vida e gestao de segredos em producao.

## Decisoes Futuras

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Refresh tokens devem migrar para cookies HttpOnly?
- O armazenamento de tokens no frontend deve continuar em `localStorage` no MVP?
- O TTL dos tokens deve variar por papel de usuario ou ambiente?
