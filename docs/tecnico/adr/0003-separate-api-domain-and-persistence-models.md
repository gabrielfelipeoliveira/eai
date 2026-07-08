# ADR 0003: Separar Modelos de API, Dominio e Persistencia

## Status

Aceita

## Contexto

O projeto deve evitar acoplamento entre contratos HTTP, conceitos de negocio e detalhes de persistencia. Cada modelo muda por motivos diferentes.

## Decisao

O EAI separa:

- DTOs de requisicao e resposta da API.
- Entidades e conceitos de dominio.
- Entidades JPA de persistencia.

Mapeamentos entre camadas devem ser explicitos. MapStruct pode ser usado quando o mapeamento se tornar nao trivial.

## Consequencias

- Contratos da API podem evoluir sem vazar detalhes do banco.
- Codigo de dominio permanece independente de anotacoes JPA.
- Adapters de persistencia sao donos da conversao entre dominio e banco.
- Alguma duplicacao de mapeamento e aceitavel quando preserva limites limpos.

## Decisoes Futuras

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Quando o mapeamento deve sair de metodos manuais e ir para MapStruct?
- Assemblers de resposta devem ser introduzidos para respostas enriquecidas?
