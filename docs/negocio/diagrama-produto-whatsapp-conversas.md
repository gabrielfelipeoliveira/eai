# Diagrama De Produto: WhatsApp E Conversas

Este documento descreve o fluxo de produto para atendimento por WhatsApp e gestao de conversas.

Objetivo do fluxo:

- Centralizar conversas de WhatsApp vinculadas a leads ou contatos.
- Permitir que vendedores atendam suas proprias conversas.
- Permitir que gerentes auditem conversas da equipe.
- Permitir que admins tenham visao completa.
- Registrar acessos gerenciais e administrativos para auditoria.

## Atores

- Cliente: pessoa que envia ou recebe mensagens pelo WhatsApp.
- Vendedor: usuario responsavel pelo atendimento comercial.
- Gerente: usuario que acompanha conversas da equipe para gestao.
- Admin: usuario com visao geral da operacao.
- WhatsApp Cloud API: provedor externo de mensagens.

## Jornada Do Atendimento

```mermaid
journey
    title Jornada de atendimento por WhatsApp
    section Entrada da conversa
      Cliente envia mensagem: 5: Cliente
      WhatsApp entrega webhook ao EAI: 4: WhatsApp Cloud API
      EAI registra contato, conversa e mensagem: 5: Sistema
      EAI vincula lead por telefone quando encontra correspondencia: 4: Sistema
    section Atendimento do vendedor
      Vendedor acessa Conversas: 5: Vendedor
      Vendedor visualiza apenas suas conversas: 5: Vendedor
      Vendedor le historico e mensagens recebidas viram lidas: 4: Vendedor
      Vendedor responde dentro da janela de 24 horas: 5: Vendedor
      Fora da janela, vendedor usa template aprovado: 4: Vendedor
    section Gestao
      Gerente filtra por vendedor, status e periodo: 5: Gerente
      Gerente abre conversa da equipe para auditoria: 5: Gerente
      Sistema registra acesso do gerente: 5: Sistema
      Admin visualiza conversas da operacao: 5: Admin
```

## Visao Do Fluxo De Produto

```mermaid
flowchart TD
    Lead[Lead ou contato WhatsApp] --> Conversation[Conversa]
    Conversation --> Inbox[Lista de Conversas]

    Inbox --> SellerView[Visao do vendedor]
    Inbox --> ManagerView[Visao do gerente]
    Inbox --> AdminView[Visao do admin]

    SellerView --> SellerScope[Apenas conversas sob sua responsabilidade]
    ManagerView --> TeamScope[Conversas da equipe no escopo do tenant]
    AdminView --> AllScope[Todas as conversas]

    SellerScope --> Attend[Atendimento]
    TeamScope --> Audit[Auditoria e gestao]
    AllScope --> Audit

    Attend --> FreeText{Mensagem recebida nos ultimos 24h?}
    FreeText -->|Sim| SendText[Enviar texto livre]
    FreeText -->|Nao| SendTemplate[Usar template aprovado]

    Audit --> Filters[Filtros: vendedor, status da ultima mensagem, periodo]
    Audit --> AccessLog[Registro de acesso gerente/admin]
```

## Permissoes Do Fluxo

| Perfil | Pode listar conversas | Escopo | Acesso auditado |
| --- | --- | --- | --- |
| `SELLER` | Sim | Apenas conversas sob sua responsabilidade | Nao |
| `MANAGER` | Sim | Conversas da equipe no escopo de tenant | Sim |
| `ADMIN` | Sim | Todas as conversas | Sim |
| `RECEPTIONIST` | Nao definido neste fluxo | Pendente | Pendente |
| `AUDITOR` | Nao definido neste fluxo | Pendente | Pendente |

## Estados E Indicadores Visiveis

Hoje o produto exibe status tecnico de mensagem, nao status proprio de conversa.

Status de mensagem usados na tela:

- `RECEIVED`: mensagem recebida do cliente.
- `SENT`: mensagem enviada pela plataforma.
- `DELIVERED`: mensagem entregue pelo provedor.
- `READ`: mensagem lida.
- `FAILED`: falha de envio.

Indicadores de gestao:

- Total de conversas retornadas pelo filtro.
- Total de mensagens nao lidas.
- Ultima mensagem.
- Data e hora da ultima interacao.
- Responsavel pela conversa.

## Fluxo De Auditoria

```mermaid
sequenceDiagram
    participant Manager as Gerente/Admin
    participant UI as Tela Conversas
    participant System as EAI
    participant Audit as Trilha de auditoria

    Manager->>UI: Filtra conversas da equipe
    UI->>System: Solicita lista filtrada
    System-->>UI: Retorna conversas permitidas
    Manager->>UI: Abre historico da conversa
    UI->>System: Solicita mensagens
    System->>Audit: Registra actor, papel, conversa, tenant e horario
    System-->>UI: Retorna historico
```

## Pendencias De Produto

- Definir se conversa deve ter status proprio ou se os filtros devem continuar usando status da ultima mensagem.
- Definir o comportamento de `AUDITOR` e `RECEPTIONIST` no contexto de conversas.
- Definir se gerente sem loja vinculada pode auditar todas as lojas da empresa.
- Definir se conversas sem vendedor responsavel devem aparecer para gerente/admin.
- Definir quais eventos de auditoria precisam aparecer em tela ou relatorio.
