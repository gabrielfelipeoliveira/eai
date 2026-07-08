# Guia Do Agente De Produto

## Resumo Do Produto

EAI e uma plataforma SaaS para lojas de veiculos e concessionarias. Ela ajuda equipes a gerenciar leads, vendedores, comunicacao por WhatsApp, etapas do funil de vendas, atividades de atendimento e relatorios comerciais em um unico ambiente operacional.

## Objetivo De Negocio

Aumentar a conversao de leads, reduzir tempo de resposta, organizar performance de vendedores e dar aos gestores visibilidade confiavel sobre o processo comercial da loja.

## Documentos Oficiais De Produto

- `docs/negocio/vision.md`
- `docs/negocio/business-rules.md`
- `docs/negocio/pendencias.md`
- `docs/negocio/domain.md`
- `docs/negocio/use-cases.md`
- `docs/negocio/roadmap.md`

## Glossario

- Empresa: organizacao cliente que usa o EAI.
- Loja: unidade comercial que pertence a uma empresa.
- Usuario: pessoa que autentica e opera no EAI.
- Vendedor: usuario responsavel por contato comercial e negociacao.
- Gerente: usuario responsavel por supervisao operacional.
- Lead: oportunidade comercial de um potencial cliente.
- Funil: progressao de status de um lead.
- Atribuicao: vinculo de responsabilidade entre lead e vendedor.
- SLA: tempo esperado para atribuicao ou primeiro contato.
- Follow-up: acao futura agendada relacionada a um lead.
- Tag: etiqueta associada a um lead.
- Template: mensagem reutilizavel para cliente.
- Comunicacao: artefato de contato registrado.
- Duplicidade: lead potencialmente repetido.

## Fluxos Conhecidos

- Usuario faz login e recebe tokens.
- Admin gerencia empresas, lojas, usuarios e vinculos de tenant.
- Leads sao criados manualmente ou por origens automaticas.
- Leads avancam por status do funil.
- Leads podem ser atribuidos manual ou automaticamente.
- Usuarios podem registrar observacoes, tags, comunicacoes e follow-ups.
- Gerentes e admins monitoram distribuicao, SLA e metricas de dashboard.
- Contas de e-mail podem importar leads por IMAP.

## Regras Conhecidas Hoje

As regras sao mantidas em `docs/negocio/business-rules.md`. Agentes nao devem adicionar comportamento nao documentado durante implementacao.

## Pendencias De Produto

Antes de implementar comportamento de produto, revise `docs/negocio/pendencias.md`.

Os demais documentos de negocio devem conter principalmente informacoes definidas. Quando uma regra for aprovada, mova a decisao para o documento oficial adequado e remova ou marque a pendencia como resolvida.

## Regras Do Agente De Produto

- Nao invente comportamento ausente.
- Converta ambiguidades em perguntas para o Product Owner.
- Mantenha o vocabulario de negocio consistente com `docs/negocio/domain.md`.
- Quando uma regra for aprovada, atualize `docs/negocio/business-rules.md` e os casos de uso afetados antes da implementacao.
