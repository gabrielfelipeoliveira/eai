# Diagrama De Produto

Este documento resume a visao de produto do EAI a partir dos documentos de negocio atuais. Ele nao define novas regras; fluxos, permissoes e metricas ainda pendentes continuam registrados em [Pendencias de produto](pendencias.md).

## Visao Geral

![Visao geral do produto](assets/diagrama-produto-visao-geral.svg)

```mermaid
flowchart LR
    subgraph Atores["Atores"]
        ADMIN["Admin"]
        MANAGER["Gerente geral"]
        STORE_MANAGER["Gerente de loja"]
        SELLER["Vendedor"]
        PRE_SALES["Pre-venda"]
        FANDI["F&I"]
    end

    subgraph EAI["EAI - Plataforma SaaS Automotiva"]
        AUTH["Autenticacao e autorizacao"]
        TENANT["Empresas, lojas e usuarios"]
        LEADS["Gestao de leads"]
        PIPELINE["Pipeline comercial"]
        CONVERSATIONS["Conversas de WhatsApp"]
        TEMPLATES["Templates de WhatsApp"]
        EMAIL["Importacao de leads por e-mail"]
        LGPD["Fluxo LGPD basico"]
    end

    subgraph Saidas["Resultados Esperados"]
        RESPONSE["Resposta mais rapida aos leads"]
        OWNERSHIP["Responsabilidade clara do vendedor"]
        HISTORY["Historico comercial centralizado"]
        VISIBILITY["Visibilidade operacional do funil"]
    end

    ADMIN --> AUTH
    MANAGER --> AUTH
    STORE_MANAGER --> AUTH
    SELLER --> AUTH
    PRE_SALES --> AUTH
    FANDI --> AUTH

    AUTH --> TENANT
    TENANT --> LEADS
    EMAIL --> LEADS
    LEADS --> PIPELINE
    LEADS --> CONVERSATIONS
    LEADS --> TEMPLATES
    LEADS --> LGPD

    PIPELINE --> OWNERSHIP
    CONVERSATIONS --> RESPONSE
    TEMPLATES --> HISTORY
    PIPELINE --> VISIBILITY
```

## Jornada Operacional

![Jornada operacional do lead](assets/diagrama-produto-jornada-operacional.svg)

```mermaid
flowchart TD
    CAPTURE["Lead captado ou criado manualmente"]
    DEDUPE["Verificacao de duplicidade quando aplicavel"]
    CREATED["Lead registrado no tenant da loja"]
    ASSIGN["Atribuicao manual ou lead disponivel"]
    CONTACT["Primeiro contato e comunicacao"]
    FUNNEL["Evolucao no funil comercial"]
    FANDI["Simulacao e proposta quando aplicavel"]
    OUTCOME["Venda, perda ou duplicidade"]
    HISTORY["Historico de origem e movimentacoes"]

    CAPTURE --> DEDUPE
    DEDUPE --> CREATED
    CREATED --> ASSIGN
    ASSIGN --> CONTACT
    CONTACT --> FUNNEL
    FUNNEL --> FANDI
    FANDI --> FUNNEL
    FUNNEL --> OUTCOME
    OUTCOME --> HISTORY
    FUNNEL --> HISTORY
```

## Capacidades Por Contexto

| Contexto | Capacidades documentadas |
| --- | --- |
| Identidade e acesso | Login, renovacao de sessao, logout e controle por papeis conhecidos. |
| Tenancy | Empresas, lojas, usuarios e escopo de visibilidade por tenant. |
| Leads | Criacao, pesquisa, atualizacao, atribuicao manual, status, notas, tags, historico e duplicidade por telefone/loja. |
| Pipeline | Visualizacao de leads agrupados por status, incluindo etapas opcionais do MVP. |
| Conversas de WhatsApp | Conversas por loja, fila da loja quando sem vendedor e dono responsavel. |
| Comunicacao | Templates globais da empresa, templates especificos da loja e envio por WhatsApp. |
| Importacao por e-mail | Contas IMAP por loja, sincronizacao manual, origem `LeadSource` e duplicidade por telefone/loja. |
| LGPD | Fluxo administrativo basico para exclusao, anonimizacao ou bloqueio quando aplicavel. |
| Segunda fase | Distribuicao automatica, SLA, follow-ups, notificacoes, KPIs, relatorios gerenciais e funil configuravel. |

## Limites Conhecidos

Ficam fora do escopo atual, conforme a visao de produto:

- Gestao de pagamentos, assinatura e billing.
- BI avancado ou data warehouse.
- Aplicativos mobile nativos.
- Parsers especificos por marketplace ficam fora do MVP.
- Distribuicao automatica, SLA, follow-ups, notificacoes e relatorios gerenciais ficam fora do MVP.
