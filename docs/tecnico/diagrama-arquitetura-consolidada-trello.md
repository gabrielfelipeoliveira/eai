# Diagrama De Arquitetura Consolidada

Diagrama tecnico baseado nas decisoes consolidadas do Trello em `docs/negocio/decisoes-consolidadas-trello.md`.

Este documento descreve a arquitetura alvo para orientar documentacao tecnica futura. Ele nao substitui ADRs nem autoriza implementacao fora da Sprint 0.

## Visao De Contexto

```mermaid
flowchart LR
    Users["Usuarios autenticados<br/>ADMIN, MANAGER, STORE_MANAGER,<br/>SELLER, PRE_SALES, F_AND_I, AVALIADOR"]
    Browser["Frontend Web<br/>React/Vite"]
    Backend["Backend API<br/>Spring Boot"]
    DB[("PostgreSQL")]
    Meta["WhatsApp Cloud API<br/>Meta"]
    Bucket[("S3 ou bucket equivalente<br/>Midias WhatsApp")]
    Mailbox["Contas IMAP<br/>Gmail, Outlook, outros"]
    Marketplaces["Fontes de lead por e-mail<br/>Webmotors, iCarros, outros"]

    Users --> Browser
    Browser -->|REST autenticado| Backend
    Backend --> DB
    Backend -->|Envio template/texto<br/>webhook status| Meta
    Meta -->|Webhook mensagem/status| Backend
    Backend -->|Upload/download midia| Bucket
    Backend -->|Importacao IMAP| Mailbox
    Marketplaces -->|E-mails de lead| Mailbox
```

## Componentes Alvo No Backend

```mermaid
flowchart TB
    subgraph API["api"]
        AuthController["Auth controllers"]
        UserController["Usuarios e permissoes"]
        LeadController["Leads e pipeline"]
        ConversationController["Conversas WhatsApp"]
        TemplateController["Templates"]
        EmailController["Contas/importacao e-mail"]
        LgpdController["Solicitacoes LGPD"]
        WebhookController["Webhook WhatsApp"]
    end

    subgraph Application["application"]
        AuthUseCases["Casos de uso de autenticacao<br/>sessao unica, refresh, logout"]
        AccessPolicy["Politicas de acesso<br/>papel + tenant"]
        UserUseCases["Gestao de usuarios"]
        LeadUseCases["Gestao de leads"]
        PipelineUseCases["Movimentacao de status"]
        ConversationUseCases["Conversas e mensagens"]
        TemplateUseCases["Templates e placeholders"]
        EmailUseCases["Importacao de e-mail"]
        LgpdUseCases["Tratamento manual LGPD"]
        AuditUseCases["Auditoria tecnica"]
        Ports["Portas de aplicacao"]
    end

    subgraph Domain["domain"]
        Company["Empresa"]
        Store["Loja"]
        User["Usuario e Papel"]
        Lead["Lead"]
        Item["Item"]
        Vehicle["Veiculo"]
        Conversation["Conversa"]
        Message["Mensagem"]
        Template["Template"]
        EmailAccount["Conta de e-mail"]
        LgpdRequest["Solicitacao LGPD"]
    end

    subgraph Infrastructure["infrastructure"]
        JpaAdapters["Adapters JPA"]
        JwtAdapter["JWT e refresh tokens"]
        MetaClient["Cliente WhatsApp Cloud API"]
        MediaStorage["Storage S3/bucket"]
        ImapReader["Leitor IMAP"]
        Encryption["Criptografia de credenciais"]
        Clock["Clock/IDs/configuracao"]
    end

    API --> Application
    Application --> Domain
    Application --> Ports
    Infrastructure --> Ports
    Infrastructure --> Domain

    AuthController --> AuthUseCases
    UserController --> UserUseCases
    LeadController --> LeadUseCases
    LeadController --> PipelineUseCases
    ConversationController --> ConversationUseCases
    TemplateController --> TemplateUseCases
    EmailController --> EmailUseCases
    LgpdController --> LgpdUseCases
    WebhookController --> ConversationUseCases

    AuthUseCases --> AccessPolicy
    UserUseCases --> AccessPolicy
    LeadUseCases --> AccessPolicy
    PipelineUseCases --> AccessPolicy
    ConversationUseCases --> AccessPolicy
    TemplateUseCases --> AccessPolicy
    EmailUseCases --> AccessPolicy
    LgpdUseCases --> AccessPolicy
```

## Fluxo De Captacao E Atendimento

```mermaid
sequenceDiagram
    participant Origem as WhatsApp/E-mail/Manual
    participant API as API Spring
    participant App as Application services
    participant Domain as Dominio
    participant DB as PostgreSQL
    participant Meta as WhatsApp Cloud API
    participant Bucket as S3/Bucket

    Origem->>API: Entrada de lead ou mensagem
    API->>App: Normaliza entrada e aplica tenant
    App->>Domain: Cria/atualiza Lead, Item, Conversa e Historico
    App->>DB: Persiste lead, origem, status e historico

    alt Midia WhatsApp
        App->>Bucket: Armazena arquivo
        App->>DB: Salva metadados e referencia do bucket
    end

    alt Template aprovado
        App->>Domain: Preenche placeholders automaticamente
        App->>Meta: Envia template pelo nome aprovado
        Meta-->>API: Webhook de status
        API->>App: Registra dados de status da Meta
        App->>DB: Atualiza mensagem/eventos
    end
```

## Modelo De Dados Conceitual

```mermaid
erDiagram
    companies ||--o{ stores : owns
    companies ||--o{ users : has
    stores ||--o{ users : allocates
    stores ||--o{ leads : receives
    users ||--o{ leads : responsible_for
    leads ||--o{ lead_history : has
    leads ||--o{ lead_notes : has
    leads ||--o{ lead_observations : has
    leads }o--o{ tags : tagged_with
    leads ||--o{ lead_phones : has
    leads ||--o{ items : references
    items ||--o{ vehicles : has
    leads ||--o| conversations : may_have
    stores ||--o{ conversations : scopes
    conversations ||--o{ conversation_messages : contains
    conversation_messages ||--o{ message_status_events : has
    conversation_messages ||--o{ message_media : may_have
    companies ||--o{ message_templates : owns
    stores ||--o{ message_templates : owns
    stores ||--o{ email_accounts : has
    email_accounts ||--o{ email_import_history : produces
    companies ||--o{ lgpd_requests : has

    users {
        uuid id
        uuid company_id
        uuid store_id
        string role
        boolean active
    }

    leads {
        uuid id
        uuid company_id
        uuid store_id
        uuid responsible_user_id
        string status
        string source
        string name
        string whatsapp_phone_e164
    }

    message_templates {
        uuid id
        uuid company_id
        uuid store_id
        string meta_template_name
        string language_code
        boolean active
        boolean deleted_logically
    }

    lgpd_requests {
        uuid id
        uuid company_id
        string request_type
        uuid executed_by_admin_id
        datetime executed_at
        string action_applied
    }
```

## Regras Arquiteturais Derivadas

- A API deve delegar regras de negocio para a camada de aplicacao.
- Politicas de papel e tenant devem ser reutilizaveis entre leads, conversas, usuarios, templates, e-mail e LGPD.
- Dominio nao deve depender de Spring, JPA, DTOs HTTP, clientes Meta, IMAP ou S3.
- Persistencia, WhatsApp Cloud API, IMAP, criptografia e bucket devem ser adapters de infraestrutura.
- Midias WhatsApp devem ser referenciadas no banco e armazenadas fora dele em S3/bucket.
- Dados de status da Meta devem ser persistidos como eventos ou dados de rastreio tecnico.
- Exclusao logica deve ser usada para templates ja utilizados.
- Dados historicos de empresas, lojas, leads e importacoes devem ser preservados quando houver desativacao ou exclusao logica.
- Automacoes irreversiveis de LGPD nao entram no MVP.

## Fronteiras De Fase Posterior

```mermaid
flowchart LR
    MVP["MVP"] --> Later["Fase posterior"]

    Later --> AutoDistribution["Distribuicao automatica"]
    Later --> SLA["SLA e escalonamento"]
    Later --> FollowUps["Follow-ups e notificacoes"]
    Later --> Reports["KPIs, dashboards e relatorios"]
    Later --> AuditUI["Tela de auditoria"]
    Later --> AuditorRole["Escopo de AUDITOR"]
    Later --> FunnelConfig["Funil configuravel"]
    Later --> LgpdAutomation["Automacoes LGPD irreversiveis"]
    Later --> RetentionPolicy["Politica formal de retencao"]
```
