# Diagrama De Produto Consolidado

Diagrama baseado nas decisoes consolidadas do Trello em [Decisoes Consolidadas Do Trello](decisoes-consolidadas-trello.md).

Este documento nao introduz novas regras. Ele organiza visualmente o escopo definido para MVP e fase posterior.

## Visao Geral Do Produto

```mermaid
flowchart LR
    subgraph Atores["Papeis do produto"]
        ADMIN["ADMIN"]
        MANAGER["MANAGER<br/>Gerente geral"]
        STORE_MANAGER["STORE_MANAGER<br/>Gerente de loja"]
        PRE_SALES["PRE_SALES<br/>Pre-venda"]
        SELLER["SELLER<br/>Vendedor"]
        FANDI["F_AND_I"]
        AVALIADOR["AVALIADOR"]
    end

    subgraph Tenancy["Tenancy"]
        COMPANY["Empresa<br/>Agrupador de lojas"]
        STORE["Loja<br/>Unidade operacional"]
        USER["Usuario<br/>Um papel por usuario"]
    end

    subgraph Captacao["Captacao"]
        WHATSAPP["WhatsApp<br/>Por loja"]
        EMAIL["E-mail<br/>Webmotors, iCarros e outros"]
        MANUAL["Criacao manual"]
    end

    subgraph Operacao["Operacao comercial"]
        LEAD["Lead<br/>Representa cliente"]
        ITEM["Item<br/>Pertence ao usuario"]
        VEHICLE["Veiculo estruturado<br/>Filho de Item"]
        PIPELINE["Pipeline<br/>Drag and drop"]
        CONVERSATION["Conversas<br/>Dono responsavel"]
        TEMPLATE["Templates WhatsApp<br/>Empresa ou loja"]
        NOTES["Notas, observacoes,<br/>tags e historico"]
    end

    subgraph Governanca["Governanca"]
        AUTH["Autenticacao<br/>Sessao unica"]
        LGPD["LGPD basica<br/>Tratamento manual por ADMIN"]
        AUDIT["Auditoria tecnica<br/>Tela futura"]
    end

    subgraph Saidas["Resultados"]
        OWNERSHIP["Responsabilidade clara"]
        HISTORY["Historico preservado"]
        TRACEABILITY["Rastreabilidade de origem"]
        CONTROL["Controle de acesso por papel"]
    end

    ADMIN --> AUTH
    MANAGER --> AUTH
    STORE_MANAGER --> AUTH
    PRE_SALES --> AUTH
    SELLER --> AUTH
    FANDI --> AUTH
    AVALIADOR --> AUTH

    COMPANY --> STORE
    COMPANY --> USER
    STORE --> USER
    STORE --> WHATSAPP
    STORE --> EMAIL

    WHATSAPP --> LEAD
    EMAIL --> LEAD
    MANUAL --> LEAD
    LEAD --> ITEM
    ITEM --> VEHICLE
    LEAD --> PIPELINE
    LEAD --> CONVERSATION
    LEAD --> NOTES
    TEMPLATE --> CONVERSATION

    AUTH --> CONTROL
    PIPELINE --> OWNERSHIP
    NOTES --> HISTORY
    CONVERSATION --> HISTORY
    EMAIL --> TRACEABILITY
    WHATSAPP --> TRACEABILITY
    LGPD --> HISTORY
    AUDIT --> TRACEABILITY
```

## Jornada Do Lead

```mermaid
flowchart TD
    START["Lead entra por WhatsApp, e-mail ou criacao manual"]
    SOURCE["Registrar origem e dados obrigatorios<br/>nome, WhatsApp e dados do anuncio"]
    DEDUPE["Verificar duplicidade por telefone/WhatsApp e loja"]
    NEWLEAD{"Novo clique em anuncio?"}
    CREATE["Criar novo lead<br/>mantendo rastreio historico"]
    AVAILABLE["Lead disponivel no pipeline"]
    ASSIGN["Atribuicao manual<br/>ou vendedor assume"]
    CONTACT["Atendimento e primeiro contato"]
    PIPELINE["Movimentacao no pipeline<br/>inclui drag and drop"]
    OPTIONAL["Etapas opcionais<br/>visita, simulacao, proposta aprovada"]
    OUTCOME{"Resultado"}
    SOLD["Vendido"]
    LOST["Perdido"]
    RECONTACT["Recontato pode reativar<br/>vendido ou perdido"]
    HISTORY["Historico preservado<br/>notas, observacoes, tags e status"]

    START --> SOURCE
    SOURCE --> DEDUPE
    DEDUPE --> NEWLEAD
    NEWLEAD -- "Sim" --> CREATE
    NEWLEAD -- "Nao" --> AVAILABLE
    CREATE --> AVAILABLE
    AVAILABLE --> ASSIGN
    ASSIGN --> CONTACT
    CONTACT --> PIPELINE
    PIPELINE --> OPTIONAL
    OPTIONAL --> PIPELINE
    PIPELINE --> OUTCOME
    OUTCOME --> SOLD
    OUTCOME --> LOST
    SOLD --> RECONTACT
    LOST --> RECONTACT
    RECONTACT --> AVAILABLE
    SOLD --> HISTORY
    LOST --> HISTORY
    PIPELINE --> HISTORY
```

## Escopo Por Papel

```mermaid
flowchart TB
    ADMIN["ADMIN<br/>Global"] --> ADMIN_CAP["Usuarios, empresas, lojas,<br/>templates, e-mail, LGPD e configuracoes"]
    MANAGER["MANAGER<br/>Empresa"] --> MANAGER_CAP["Gestao no escopo da empresa,<br/>usuarios, lojas, leads e redistribuicao"]
    STORE_MANAGER["STORE_MANAGER<br/>Loja"] --> STORE_CAP["Operacao gerencial da loja,<br/>leads e atribuicoes no escopo"]
    PRE_SALES["PRE_SALES<br/>Loja"] --> PRE_CAP["Gera leads e faz primeiro atendimento"]
    SELLER["SELLER<br/>Loja"] --> SELLER_CAP["Assume leads disponiveis<br/>e atende apenas seus leads"]
    FANDI["F_AND_I<br/>Loja"] --> FANDI_CAP["Participa de simulacao<br/>e proposta"]
    AVALIADOR["AVALIADOR<br/>Loja"] --> AVAL_CAP["Papel formal do produto;<br/>permissoes especificas a detalhar"]
```

## Itens Fora Do MVP

```mermaid
mindmap
  root((Fase posterior))
    Distribuicao automatica
    SLA
    Follow-ups
    Notificacoes
    KPIs e relatorios gerenciais
    Dashboard gerencial completo
    Parsers dedicados
    Funil configuravel por empresa ou loja
    Tela de auditoria
    Escopo operacional de AUDITOR
    Automacoes irreversiveis de LGPD
    Politica formal de retencao
```
