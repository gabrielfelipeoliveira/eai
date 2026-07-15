# Banco de Dados

Este documento descreve o modelo conceitual do banco e as regras de ownership. Ele nao substitui migrations e nao autoriza mudancas de schema fora do fluxo aprovado.

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
- `V2__whatsapp_conversations.sql`
- `V3__lead_communication_whatsapp_template_channel.sql`
- `V4__conversation_message_events.sql`
- `V5__conversation_access_audits.sql`

Observacao:

- A base ainda e descartavel em ambiente local.
- Depois que a base deixar de ser descartavel, migrations ja aplicadas nao devem ser modificadas; novas alteracoes devem ser criadas em novas versoes.

## Tabelas Conceituais

Grupos atuais e alvo:

- Identidade e autenticacao: `users`, `user_roles`, `refresh_tokens`.
- Tenancy: `companies`, `stores`.
- Leads: `leads`, `lead_history`, `lead_notes`, `lead_tags` e estruturas futuras para observacoes e telefones vinculados.
- Item e veiculo: `items` e `vehicles` ou equivalentes futuros.
- Mensageria: `message_templates` e comunicacoes de lead.
- WhatsApp: contatos, conversas, mensagens, eventos de status, midias e auditoria tecnica.
- E-mail: contas de e-mail e historico de importacao.
- LGPD: solicitacoes e acoes aplicadas.
- SLA e follow-ups: estruturas existentes ou futuras de apoio, sem regra operacional obrigatoria no MVP.

## Relacionamentos Conceituais

- Empresa possui lojas.
- Empresa e agrupador; loja e unidade operacional.
- Loja possui dados fiscais/operacionais como CNPJ, endereco, telefone e razao social.
- Usuario pertence a empresa e, quando operacional, a loja.
- Usuario possui um unico papel.
- Lead pertence a empresa e loja.
- Lead pode ter historico, notas, observacoes, tags e telefones vinculados.
- Lead pode se relacionar a Item e Veiculo estruturado.
- Item pertence ao usuario.
- Veiculo e filho de Item.
- Uma conversa pertence a loja e pode estar vinculada a um lead.
- Uma conversa tem muitas mensagens.
- Uma mensagem de conversa pode ter muitos eventos de status recebidos do provedor.
- Uma mensagem de conversa pode ter metadados e referencia de midia armazenada em S3/bucket.
- Um template de mensagem pertence a empresa ou loja.
- Template usado deve suportar exclusao logica.
- Uma comunicacao de lead pode referenciar um template.
- Uma conta de e-mail pertence a empresa e loja.
- Historico de importacao de e-mail deve ser preservado mesmo se a conta for excluida/desativada.
- Configuracao de distribuicao e politica de SLA sao escopadas por loja e ficam como apoio/fase posterior.
- Solicitacoes LGPD devem registrar executor, data e acao aplicada.

## Constraints e Indices

Constraints conhecidas ou desejadas:

- Chaves primarias usam UUID.
- Campos com comportamento de enum usam constraints `CHECK` ou tipo equivalente definido em migration.
- Valor de venda deve ser nulo ou nao negativo.
- Moeda deve aceitar valores alem de BRL; BRL e o default de negocio.
- Telefone deve seguir E.164 quando aplicavel.
- Contatos de WhatsApp sao unicos por loja e telefone.
- Conversas sao unicas por contato e por lead quando houver lead vinculado.
- Tags devem impedir duplicidade do mesmo tipo no mesmo lead.
- Refresh token anterior deve ser revogado quando houver rotacao.
- Sessao ativa deve respeitar a regra de no maximo uma sessao por usuario.
- Templates usados devem preservar historico por exclusao logica.
- Auditorias de acesso de conversa registram acesso tecnico de perfis gerenciais e admins enquanto a tela de auditoria fica para fase posterior.

Indices conhecidos ou desejados:

- Leads por empresa, loja, status, origem, usuario responsavel, data de criacao, veiculo/item e telefone.
- Historico, notas, observacoes e tags por lead.
- Conversas de WhatsApp por empresa, loja, lead, vendedor responsavel e data de atualizacao.
- Mensagens de conversa por conversa, data de criacao, status e identidade externa da mensagem.
- Eventos de mensagens por mensagem, identidade externa, status e data do evento.
- Importacoes de e-mail por conta, loja, status e data.
- Solicitacoes LGPD por empresa, titular quando aplicavel, status e data.

## Seeds E Dados De Demonstracao

Regras definidas:

- Dados de seed nao devem existir em deploys de producao.
- Dados de demonstracao devem ser separados de seeds obrigatorios.
- Producao nao deve receber dados de seed/demonstracao automaticamente.
- Seeds/documentacao de papeis devem incluir `ADMIN`, `MANAGER`, `STORE_MANAGER`, `SELLER`, `PRE_SALES`, `F_AND_I` e `AVALIADOR`.
- `AUDITOR` fica fora do MVP.
- Templates padrao necessarios para o MVP serao definidos depois.
- Templates padrao podem ser aprovados ou alterados por `ADMIN` e gerente geral.

## Conflitos Conhecidos

- O banco/codigo existente pode ainda conter estruturas antigas de SLA, follow-up, relatorios, dashboard ou `AUDITOR`. Esses itens devem ser tratados como legado/apoio ou fase posterior quando conflitarem com o MVP consolidado.
- Qualquer divergencia entre migrations existentes e regras consolidadas deve virar nova migration futura; nao alterar migrations ja aplicadas fora de base descartavel.

## Decisoes Futuras De Banco

Perguntas para o Software Architect:

- Testes de integracao devem usar PostgreSQL/Testcontainers em vez de H2?
- Convencoes de nomes de banco devem ser documentadas em detalhe?
- Colunas de auditoria devem ser padronizadas em todas tabelas?
- Soft delete deve ser introduzido como padrao para entidades historicas alem de templates?
- Como modelar fisicamente Item, Veiculo, observacoes historicas, telefones adicionais e midias WhatsApp sem quebrar o dominio existente?
