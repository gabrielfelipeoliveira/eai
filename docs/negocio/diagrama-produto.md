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
        AVALIADOR["Avaliador"]
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
    AVALIADOR --> AUTH

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
| Identidade e acesso | Login, sessao unica, refresh token rotativo, logout global e controle por papeis conhecidos. |
| Tenancy | Empresa como agrupador, lojas como unidades operacionais, usuarios e escopo de visibilidade por tenant. |
| Leads | Criacao, pesquisa normalizada, atualizacao, atribuicao manual, status, notas, observacoes, tags globais, historico e duplicidade por telefone/loja. |
| Pipeline | Visualizacao de leads agrupados por status e movimentacao por arrastar e soltar. |
| Conversas de WhatsApp | Conversas por loja, fila da loja quando sem vendedor, dono responsavel e armazenamento de midias em S3/bucket. |
| Comunicacao | Templates da empresa, templates especificos da loja, placeholders automaticos e envio por WhatsApp. |
| Importacao por e-mail | Contas IMAP por loja, sincronizacao com retentativas, origem `LeadSource`, mensagens marcadas como lidas e duplicidade por telefone/loja. |
| LGPD | Fluxo administrativo basico e manual por `ADMIN`, sem automacoes irreversiveis no MVP. |
| Segunda fase | Distribuicao automatica, SLA, follow-ups, notificacoes, KPIs, relatorios gerenciais, funil configuravel, tela de auditoria e `AUDITOR` fora do MVP. |

## Limites Conhecidos

Ficam fora do escopo atual, conforme a visao de produto:

- Gestao de pagamentos, assinatura e billing.
- BI avancado ou data warehouse.
- Aplicativos mobile nativos.
- Parsers especificos por marketplace ficam fora do MVP.
- Distribuicao automatica, SLA, follow-ups, notificacoes e relatorios gerenciais ficam fora do MVP.
