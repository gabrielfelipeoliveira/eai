# Diagrama De Arquitetura: WhatsApp E Conversas

Este documento descreve a arquitetura implementada para o fluxo de WhatsApp, conversas, auditoria e gestao de conversas.

Escopo deste diagrama:

- Webhook publico da WhatsApp Cloud API.
- Registro de contatos, conversas, mensagens e eventos de status.
- Envio de template e texto livre.
- Listagem, filtros e auditoria de acesso a conversas.
- Separacao entre frontend, API, aplicacao, dominio e infraestrutura.

Fora de escopo deste diagrama:

- Regras oficiais de multi-conta WhatsApp por empresa/loja ainda pendentes.
- Ciclo de vida proprio de status de conversa, ainda pendente de decisao de produto.
- Tela de auditoria e escopo operacional de `AUDITOR`, que ficam para fase posterior.

## Visao De Componentes

```mermaid
flowchart LR
    Meta[WhatsApp Cloud API] -->|Webhook GET/POST| Webhook[WhatsAppWebhookController]
    Bucket[(S3 ou bucket equivalente)] 
    WebApp[Frontend React/Vite] -->|REST autenticado| ConversationApi[ConversationController]
    WebApp -->|REST autenticado| LeadApi[Lead/Message Controllers]

    ConversationApi --> ConversationService[ConversationService]
    LeadApi --> TemplateSender[WhatsAppTemplateSenderService]
    ConversationApi --> TextSender[WhatsAppTextSenderService]
    Webhook --> WebhookService[WhatsAppWebhookService]

    WebhookService --> ConversationService
    TemplateSender --> ConversationService
    TextSender --> ConversationService

    ConversationService --> DomainConversation[Dominio conversation]
    ConversationService --> LeadPort[LeadRepository port]
    ConversationService --> ContactPort[WhatsAppContactRepository port]
    ConversationService --> ConversationPort[ConversationRepository port]
    ConversationService --> MessagePort[ConversationMessageRepository port]
    ConversationService --> EventPort[ConversationMessageEventRepository port]
    ConversationService --> AuditPort[ConversationAccessAuditRepository port]

    TemplateSender --> TemplateClientPort[WhatsAppTemplateClient port]
    TextSender --> TextClientPort[WhatsAppTextClient port]

    ContactPort --> ContactAdapter[JPA adapter]
    ConversationPort --> ConversationAdapter[JPA adapter]
    MessagePort --> MessageAdapter[JPA adapter]
    EventPort --> EventAdapter[JPA adapter]
    AuditPort --> AuditAdapter[JPA adapter]
    LeadPort --> LeadAdapter[JPA adapter]
    TemplateClientPort --> MetaTemplateClient[WhatsAppCloudTemplateClient]
    TextClientPort --> MetaTextClient[WhatsAppCloudText client]

    ContactAdapter --> DB[(PostgreSQL)]
    ConversationAdapter --> DB
    MessageAdapter --> DB
    EventAdapter --> DB
    AuditAdapter --> DB
    LeadAdapter --> DB
    MetaTemplateClient --> Meta
    MetaTextClient --> Meta
    MessageAdapter --> Bucket
```

## Fluxo De Mensagem Recebida

```mermaid
sequenceDiagram
    participant Meta as WhatsApp Cloud API
    participant Webhook as WhatsAppWebhookController
    participant Service as WhatsAppWebhookService
    participant Conversations as ConversationService
    participant DB as PostgreSQL

    Meta->>Webhook: POST /api/webhooks/whatsapp
    Webhook->>Service: processa payload bruto
    Service->>Conversations: recordIncomingMessage(companyId, storeId, message)
    Conversations->>DB: busca contato por loja + telefone
    Conversations->>DB: busca lead por telefone da loja
    Conversations->>DB: cria/atualiza contato WhatsApp
    Conversations->>DB: cria/atualiza conversa
    Conversations->>DB: grava mensagem INBOUND RECEIVED
    Webhook-->>Meta: 200 OK
```

## Fluxo De Envio

```mermaid
sequenceDiagram
    participant User as Usuario autenticado
    participant UI as Frontend Conversas/Leads
    participant API as API Spring
    participant Sender as WhatsApp sender service
    participant Meta as WhatsApp Cloud API
    participant Conversations as ConversationService
    participant DB as PostgreSQL

    alt Template aprovado
        User->>UI: Envia template para lead
        UI->>API: POST /api/leads/{id}/whatsapp-template
        API->>Sender: valida acesso, lead e template
        Sender->>Meta: envia template
        Sender->>Conversations: recordOutboundMessage(...)
        Conversations->>DB: grava mensagem OUTBOUND TEMPLATE SENT ou FAILED
        API-->>UI: resposta com status e ids
    else Texto livre
        User->>UI: Envia texto na conversa
        UI->>API: POST /api/conversations/{id}/messages
        API->>Sender: valida acesso e janela de 24 horas
        Sender->>Meta: envia texto
        Sender->>Conversations: recordOutboundMessage(...)
        Conversations->>DB: grava mensagem OUTBOUND TEXT SENT ou FAILED
        API-->>UI: resposta com status e ids
    end
```

## Fluxo De Gestao E Auditoria

```mermaid
sequenceDiagram
    participant Gestor as Gerente/Admin
    participant UI as Frontend Conversas
    participant API as ConversationController
    participant Service as ConversationService
    participant DB as PostgreSQL

    Gestor->>UI: Aplica filtros de vendedor/status/periodo
    UI->>API: GET /api/conversations?sellerId=&messageStatus=&startAt=&endAt=
    API->>Service: listConversationSummaries(authenticatedUser, filters)
    Service->>DB: carrega conversas por escopo do perfil
    Service->>Service: aplica filtros e ordena por ultima interacao
    API-->>UI: resumos visiveis ao usuario

    Gestor->>UI: Abre historico de uma conversa
    UI->>API: GET /api/conversations/{id}/messages
    API->>Service: listMessages(id, authenticatedUser)
    Service->>Service: valida acesso por perfil e tenant
    Service->>DB: grava conversation_access_audits
    Service->>DB: marca INBOUND RECEIVED como READ
    Service->>DB: carrega mensagens
    API-->>UI: mensagens cronologicas
```

## Regras Tecnicas Relevantes

- `SELLER` lista apenas conversas em que `responsibleUserId` e o proprio usuario.
- `MANAGER` lista conversas do escopo de tenant atual.
- `ADMIN` lista todas as conversas.
- A API recebe filtros `sellerId`, `messageStatus`, `startAt` e `endAt`.
- `messageStatus` filtra o status da ultima mensagem, pois o dominio ainda nao possui status proprio de conversa.
- Abertura de detalhe ou mensagens por `ADMIN` e `MANAGER` gera registro em `conversation_access_audits`.
- A leitura de mensagens marca mensagens recebidas com status `RECEIVED` como `READ`.
- Dados de status recebidos da Meta devem ser preservados para rastreio tecnico.
- Midias de WhatsApp devem ser armazenadas em S3 ou bucket equivalente, com metadados e referencia persistidos no banco.
- Tela de auditoria fica para fase posterior.

## Tabelas Envolvidas

```mermaid
erDiagram
    companies ||--o{ stores : owns
    stores ||--o{ whatsapp_contacts : scopes
    whatsapp_contacts ||--o| conversations : has
    conversations ||--o{ conversation_messages : contains
    conversation_messages ||--o{ conversation_message_events : has
    conversations ||--o{ conversation_access_audits : audited_by
    leads ||--o| conversations : may_link
    users ||--o{ conversations : responsible
    users ||--o{ conversation_access_audits : actor

    conversations {
        uuid id
        uuid company_id
        uuid store_id
        uuid contact_id
        uuid lead_id
        uuid responsible_user_id
        timestamptz created_at
        timestamptz updated_at
    }

    conversation_messages {
        uuid id
        uuid conversation_id
        varchar direction
        varchar type
        varchar status
        varchar external_message_id
        text content
        text raw_payload
        timestamptz created_at
        timestamptz updated_at
    }

    conversation_access_audits {
        uuid id
        uuid conversation_id
        uuid company_id
        uuid store_id
        uuid lead_id
        uuid actor_user_id
        varchar actor_role
        varchar access_type
        timestamptz accessed_at
    }
```

## Pendencias Relacionadas

- Definir se conversa deve ter status proprio.
- Definir mapeamento oficial multi-conta WhatsApp por empresa/loja/numero.
- Definir tratamento de mensagens recebidas sem lead vinculado.
- Definir politica de retencao e consulta dos registros de auditoria.
