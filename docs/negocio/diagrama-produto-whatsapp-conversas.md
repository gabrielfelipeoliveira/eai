# Diagrama De Produto: WhatsApp E Conversas

Este documento descreve o fluxo de produto para atendimento por WhatsApp e gestao de conversas.

Objetivo do fluxo:

- Centralizar conversas de WhatsApp vinculadas a leads ou contatos.
- Permitir que vendedores atendam conversas sob sua responsabilidade.
- Manter conversas sem vendedor na fila da loja.
- Permitir que gerentes supervisionem conversas da equipe.
- Permitir que gerentes respondam somente quando assumirem o lead.
- Permitir que admins tenham visao global.

## Atores

- Cliente: pessoa que envia ou recebe mensagens pelo WhatsApp.
- Vendedor: usuario responsavel pelo atendimento comercial.
- Gerente geral: usuario que acompanha conversas da empresa.
- Gerente de loja: usuario que acompanha conversas da loja.
- Admin: usuario global da plataforma.
- WhatsApp Cloud API: provedor externo de mensagens.

## Jornada Do Atendimento

```mermaid
journey
    title Jornada de atendimento por WhatsApp
    section Entrada da conversa
      Cliente envia mensagem: 5: Cliente
      WhatsApp entrega webhook ao EAI: 4: WhatsApp Cloud API
      EAI registra contato, conversa e mensagem: 5: Sistema
      EAI vincula lead por telefone e loja quando encontra correspondencia: 4: Sistema
      Conversa sem vendedor fica na fila da loja: 5: Sistema
    section Atendimento do vendedor
      Vendedor assume lead disponivel: 5: Vendedor
      Vendedor acessa Conversas: 5: Vendedor
      Vendedor visualiza conversas sob sua responsabilidade: 5: Vendedor
      Vendedor le historico e mensagens recebidas viram lidas: 4: Vendedor
      Vendedor responde dentro da janela de 24 horas: 5: Vendedor
      Fora da janela, vendedor usa template aprovado: 4: Vendedor
    section Gestao
      Gerente supervisiona conversas do escopo: 5: Gerente
      Gerente assume lead quando precisa responder: 4: Gerente
      Admin visualiza conversas da operacao: 5: Admin
```

## Visao Do Fluxo De Produto

```mermaid
flowchart TD
    Channel[Numero WhatsApp da loja] --> Lead[Lead ou contato WhatsApp]
    Lead --> Conversation[Conversa da loja]
    Conversation --> Queue{Tem dono responsavel?}

    Queue -->|Nao| StoreQueue[Fila da loja]
    Queue -->|Sim| OwnerView[Visao do responsavel]

    StoreQueue --> Assign[Assumir ou atribuir lead]
    Assign --> OwnerView

    OwnerView --> Attend[Atendimento]
    Attend --> FreeText{Mensagem recebida nos ultimos 24h?}
    FreeText -->|Sim| SendText[Enviar texto livre]
    FreeText -->|Nao| SendTemplate[Usar template aprovado]

    Conversation --> ManagerView[Supervisao gerencial]
    ManagerView --> Observe[Supervisionar]
    ManagerView --> TakeOver[Assumir lead para responder]
```

## Permissoes Do Fluxo

| Perfil | Pode listar conversas | Escopo | Pode responder |
| --- | --- | --- | --- |
| `ADMIN` | Sim | Global | Conforme regra operacional do atendimento |
| `MANAGER` | Sim | Todas as lojas da empresa | Apenas se assumir o lead |
| `STORE_MANAGER` | Sim | Loja em que esta alocado | Apenas se assumir o lead |
| `SELLER` | Sim | Conversas sob sua responsabilidade | Sim, quando for dono responsavel |
| `PRE_SALES` | Nao no MVP | Segunda fase para assumir fila | Nao no MVP |
| `F_AND_I` | Apenas quando relacionado ao lead | Etapas de simulacao e proposta | Conforme fluxo do lead |

## Estados E Indicadores Visiveis

O produto exibe status tecnico de mensagem, nao status proprio de conversa.

Status de mensagem usados na tela:

- `RECEIVED`: mensagem recebida do cliente.
- `SENT`: mensagem enviada pela plataforma.
- `DELIVERED`: mensagem entregue pelo provedor.
- `READ`: mensagem lida.
- `FAILED`: falha de envio.

Indicadores operacionais:

- Total de conversas retornadas pelo filtro.
- Total de mensagens nao lidas.
- Ultima mensagem.
- Data e hora da ultima interacao.
- Responsavel pela conversa.
- Fila da loja para conversas sem vendedor.

## Regras Definidas

- Cada loja deve ter apenas um numero de WhatsApp.
- Conversas pertencem a loja do numero de WhatsApp.
- Conversas sem vendedor ficam na fila da loja.
- Pre-venda assumir conversas da fila fica para segunda fase.
- Gerente apenas supervisiona enquanto o lead estiver no nome do vendedor.
- Gerente pode responder somente quando assumir o lead.
- Templates da empresa podem ser usados por todas as lojas.
- Templates da loja sao especificos daquela loja.

## Pendencias De Produto

- Definir se eventos de auditoria de conversas precisam aparecer em tela ou apenas ficar registrados tecnicamente.
- Definir tratamento de midias de WhatsApp: apenas metadados ou download e armazenamento.
