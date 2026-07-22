# ADR 0004: Usar Access Tokens JWT e Refresh Tokens Persistidos

## Status

Aceita

## Contexto

O EAI precisa de acesso autenticado a API para usuarios no navegador. A implementacao atual usa access tokens stateless e refresh tokens persistidos.

## Decisao

Usar access tokens JWT para chamadas autenticadas da API e persistir refresh tokens para permitir renovacao de sessao e revogacao logica.

Refresh tokens devem trafegar em cookie `HttpOnly` emitido pelo backend, com `SameSite=Strict` por padrao e `Secure=true` em producao. O frontend nao deve persistir refresh tokens em `localStorage` ou outro storage acessivel por JavaScript.

O access token pode ficar em memoria no frontend e ser reenviado em `Authorization: Bearer <token>`. Ao recarregar a pagina ou receber `401`, o frontend tenta renovar a sessao usando o cookie HttpOnly no endpoint de refresh.

## Consequencias

- Chamadas de API podem ser autenticadas sem sessoes server-side para access tokens.
- Persistencia de refresh tokens suporta logout e comportamento de revogacao.
- XSS deixa de expor refresh tokens diretamente, reduzindo o risco de roubo de sessao de longa duracao.
- Requisicoes autenticadas de navegador para login, refresh e logout devem usar credenciais CORS para envio do cookie.
- Access tokens em memoria podem ser perdidos ao recarregar a pagina; nesse caso o refresh via cookie reconstroi a sessao quando o refresh token ainda for valido.

## Decisoes Futuras

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- O TTL dos tokens deve variar por papel de usuario ou ambiente?
