# Visao Do Produto

Este documento registra a visao e o escopo conhecido do produto. Regras detalhadas ficam em [Regras de negocio](business-rules.md), e duvidas ainda abertas ficam em [Pendencias de produto](pendencias.md).

## Objetivo

O EAI e uma plataforma SaaS para lojas automotivas e concessionarias. Ela centraliza captacao de leads, atribuicao manual de responsaveis, comunicacao por WhatsApp, acompanhamento do funil comercial, historico operacional e organizacao basica da rotina comercial.

O objetivo comercial e aumentar conversao de leads, reduzir o tempo de resposta, organizar responsabilidades entre pre-venda, vendedores, gerentes e F&I, e dar aos gestores visibilidade operacional confiavel sobre o processo de vendas da loja.

## Publico-Alvo

- Donos de concessionarias e lojas automotivas.
- Gerentes gerais.
- Gerentes de loja.
- Vendedores.
- Equipes de pre-venda ou BDC.
- F&I.
- Usuarios administrativos responsaveis por empresas, lojas e usuarios.

## Problema

Times comerciais automotivos frequentemente perdem oportunidades porque captacao de leads, dono responsavel, historico de contato, conversas de WhatsApp e status do funil ficam espalhados em ferramentas diferentes ou processos informais.

O EAI existe para reduzir essa fragmentacao operacional, deixando visibilidade de leads, fila de oportunidades, responsabilidade do vendedor, historico de chegada do lead, conversas e status do funil em um unico ambiente.

## Proposta De Valor

O EAI ajuda concessionarias e lojas a:

- Responder mais rapido aos leads recebidos.
- Reduzir oportunidades perdidas ou duplicadas.
- Organizar a passagem entre pre-venda, vendedor e F&I.
- Centralizar conversas de WhatsApp por loja.
- Registrar historico de origem e duplicidade de leads.
- Acompanhar status do funil comercial.
- Dar visibilidade operacional para gerentes.
- Preparar a plataforma para regras mais avancadas de funil, relatorios, SLA e automacao em fases futuras.

## Escopo Do MVP

O MVP confirmado inclui:

- Tenancy basico: empresa, lojas e usuarios.
- Papeis e permissoes iniciais: admin, gerente geral, gerente de loja, pre-venda, vendedor e F&I.
- Captacao de leads por WhatsApp e por e-mail.
- Gestao de leads com historico de origem, duplicidade por telefone e loja, e funil comercial.
- Conversas de WhatsApp por loja, com dono responsavel e vinculo com lead.
- Templates de mensagem globais da empresa e templates especificos da loja.
- Fluxo operacional entre pre-venda, vendedor e F&I.
- Pipeline com status atuais e etapas opcionais visiveis no MVP.
- LGPD basica: processo para exclusao, anonimizacao ou bloqueio de dados pessoais quando aplicavel.

## Segunda Fase

Ficam para segunda fase:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- KPIs, dashboards e relatorios gerenciais.
- Parsers dedicados para plataformas especificas.
- Configuracao de etapas do funil por empresa ou loja.
- Regras avancadas de auditoria, retencao e compliance.

## Fora De Escopo Atual

Os itens abaixo nao estao definidos como escopo atual:

- Gestao de pagamentos, assinatura e billing.
- BI avancado ou data warehouse.
- Aplicativos mobile nativos.
- Parsers especificos por marketplace no MVP.
- Automacoes avancadas de distribuicao, notificacao e SLA no MVP.
- Politicas automaticas de expurgo de dados no MVP.
