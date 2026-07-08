# Banco de Dados

Este documento descreve o modelo conceitual do banco e as regras de ownership. Ele nao inventa novas colunas de negocio.

## Tecnologia

- PostgreSQL e o banco principal.
- Flyway e dono das migrations de schema.
- H2 e usado nos testes atuais em modo de compatibilidade com PostgreSQL.

## Ownership do Flyway

Regras:

- Toda mudanca de schema deve ser introduzida por uma nova migration Flyway.
- Migrations existentes nao devem ser modificadas depois de aplicadas fora de experimentacao local.
- Migrations devem ser deterministicas e revisaveis.
- Invariantes importantes devem usar constraints explicitas.
- Nomes de migrations devem seguir `V{numero}__descricao.sql`.

## Migrations Atuais

- `V1__initial_schema.sql`
- `V2__create_users_and_refresh_tokens.sql`
- `V3__add_companies_stores_and_user_tenant_links.sql`
- `V4__create_leads.sql`
- `V5__create_message_templates_and_lead_communications.sql`
- `V6__ensure_default_message_template.sql`
- `V7__create_email_accounts.sql`
- `V8__add_lead_distribution_and_sla.sql`
- `V9__create_follow_up_tasks.sql`

## Tabelas Conceituais

Grupos atuais:

- Identidade e autenticacao: `users`, `user_roles`, `refresh_tokens`.
- Tenancy: `companies`, `stores`.
- Leads: `leads`, `lead_history`, `lead_notes`, `lead_tags`.
- Mensageria: `message_templates`, `lead_communications`.
- Importacao por e-mail: `email_accounts`.
- Distribuicao e SLA: `lead_distribution_config`, `lead_sla_policy`.
- Follow-ups: `follow_up_tasks`.

## Relacionamentos Conceituais

- Uma empresa tem muitas lojas.
- Uma loja pertence a uma empresa.
- Um usuario pode estar vinculado a empresa e loja.
- Um usuario pode ter multiplos papeis.
- Um refresh token pertence a um usuario.
- Um lead pertence a uma empresa e a uma loja.
- Um lead pode estar atribuido a um usuario.
- Um lead tem muitos registros de historico.
- Um lead tem muitas notas.
- Um lead tem muitas tags.
- Um lead tem muitas comunicacoes.
- Um lead tem muitas tarefas de follow-up.
- Um template de mensagem pertence a uma empresa e loja.
- Uma comunicacao de lead pode referenciar um template.
- Uma conta de e-mail pertence a uma empresa e loja.
- Configuracao de distribuicao e escopada por loja.
- Politica de SLA e escopada por loja.

## Constraints e Indices

Constraints conhecidas:

- Chaves primarias usam UUID.
- Campos com comportamento de enum usam constraints `CHECK`.
- Valor de venda deve ser nulo ou nao negativo.
- Configuracao de distribuicao e unica por empresa e loja.
- Politica de SLA e unica por empresa e loja.
- Minutos de SLA devem ser positivos.
- Chaves estrangeiras reforcam relacionamentos entre tabelas.

Indices conhecidos:

- Leads possuem indices por empresa, loja, status, origem, usuario responsavel, data de criacao, veiculo e telefone.
- Historico, notas e tags possuem indices orientados ao lead.
- Tabelas de distribuicao e SLA possuem indices orientados a loja.
- Tarefas de follow-up possuem indices por lead, usuario/status/vencimento e vencimento.

## Ownership dos Dados

- O codigo de aplicacao e dono do comportamento de negocio.
- Migrations Flyway sao donas do formato do schema.
- Adapters de persistencia sao donos do mapeamento entre dominio e entidades de banco.
- DTOs de API nunca devem expor entidades de persistencia diretamente.

## Dados de Seed

Dados de seed conhecidos:

- Usuario admin de desenvolvimento.
- Empresa padrao de desenvolvimento.
- Loja padrao de desenvolvimento.
- Template de mensagem padrao.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Dados de seed devem existir em deploys de producao?
- Templates padrao devem ser criados por empresa, por loja ou globalmente?
- Dados de demonstracao devem ser separados de seeds obrigatorios do sistema?

## Conflitos Conhecidos

### Ator de Historico do Sistema

A documentacao atual afirma que historicos criados pelo scheduler usam um registro de sistema sem usuario.

A migration `V4__create_leads.sql` define `lead_history.user_id` como `NOT NULL`.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Historico de sistema deve usar um usuario tecnico dedicado?
- `lead_history.user_id` deve se tornar nullable em uma migration futura?
- O ator do historico deve ser modelado explicitamente como usuario ou sistema?

## Decisoes Futuras de Banco

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Testes de integracao devem usar PostgreSQL/Testcontainers em vez de H2?
- Convencoes de nomes de banco devem ser documentadas em detalhe?
- Colunas de auditoria devem ser padronizadas em todas as tabelas?
- Soft delete deve ser introduzido para algumas entidades?
