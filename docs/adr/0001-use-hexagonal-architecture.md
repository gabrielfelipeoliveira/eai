# ADR 0001: Usar Arquitetura Hexagonal

## Status

Aceita

## Contexto

O EAI precisa suportar crescimento de negocio em autenticacao, tenants, leads, comunicacao, importacoes, relatorios e futuras integracoes. O backend precisa de limites claros para que comportamento de negocio nao fique acoplado a HTTP, persistencia ou detalhes de framework.

## Decisao

O backend usa arquitetura hexagonal sob o pacote `com.eai`:

- `domain`: entidades, value objects e regras de dominio.
- `application`: casos de uso, portas, servicos de aplicacao e orquestracao.
- `infrastructure`: persistencia, integracoes externas, configuracao de framework e adapters.
- `api`: controllers HTTP, DTOs de requisicao, DTOs de resposta e tratamento de erro da API.

## Consequencias

- Codigo de dominio nao deve depender de Spring ou APIs de persistencia.
- Controllers nao devem conter regras de negocio.
- Entidades de persistencia nao devem vazar em respostas da API.
- Portas da aplicacao definem dependencias necessarias aos casos de uso.
- Infrastructure implementa adapters para essas portas.

## Decisoes Futuras

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Servicos de aplicacao devem continuar gerenciados pelo Spring ou anotacoes de framework devem sair da camada de aplicacao?
- Boundaries transacionais devem permanecer nos servicos de aplicacao ou ir para adapters/configuracao?
