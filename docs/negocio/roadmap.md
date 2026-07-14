# Roadmap

Este roadmap organiza a evolucao conhecida do projeto. Regras aprovadas ficam em [Regras de negocio](business-rules.md), e duvidas abertas ficam em [Pendencias de produto](pendencias.md).

## Sprint 0: Base De Engenharia

Objetivo:

- Estabelecer documentacao profissional e acordos de trabalho como fonte oficial da verdade.

Escopo:

- Criar documentacao de produto, regras de negocio, dominio, casos de uso, arquitetura, API, banco e roadmap.
- Criar estrutura de ADR e decisoes arquiteturais iniciais.
- Criar documentos de onboarding em `.agents/` para agentes de IA.
- Preservar documentacao util existente.
- Registrar decisoes de negocio pendentes em vez de assumir comportamento.

Fora de escopo:

- Novas funcionalidades.
- Migrations de banco.
- Mudancas de API.
- Grandes refactors.

## MVP Confirmado

Escopo:

- Autenticacao e autorizacao.
- Tenancy basico: empresas, lojas e usuarios.
- Papeis iniciais: `ADMIN`, `MANAGER`, `STORE_MANAGER`, `SELLER`, `PRE_SALES` e `F_AND_I`.
- Captacao de leads por WhatsApp e por e-mail.
- Cadastro, listagem e atualizacao de leads.
- Atribuicao manual de leads.
- Leads disponiveis para vendedores assumirem.
- Gerente geral atribuindo leads na empresa.
- Gerente de loja atribuindo leads na propria loja.
- Pipeline comercial com status atuais e etapas opcionais visiveis.
- Etapas de F&I: simulacao e proposta aprovada.
- Historico de origem, duplicidade e movimentacoes do lead.
- Conversas de WhatsApp por loja.
- Fila da loja para conversas iniciadas pelo lead sem vendedor.
- Templates de mensagem globais da empresa e especificos da loja.
- Importacao de leads por e-mail com origem registrada como `LeadSource`.
- Duplicidade por telefone/WhatsApp e loja.
- LGPD basica com fluxo administrativo executado inicialmente por `ADMIN`.

Fora do MVP, mas considerado segunda fase:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- KPIs, dashboards e relatorios gerenciais.
- Parsers dedicados para plataformas especificas.
- Configuracao de etapas do funil por empresa ou loja.
- Regras avancadas de auditoria, retencao e compliance.

## Versao 1

Temas candidatos:

- Implementar ou ajustar o sistema ao MVP confirmado.
- Remover papeis fora do MVP ou isola-los de fluxos operacionais.
- Endurecer permissoes e escopo de tenant conforme papeis definidos.
- Consolidar o fluxo de pre-venda, vendedor e F&I.
- Preparar arquitetura de pipeline para etapas configuraveis.
- Organizar fila de conversas da loja.
- Fortalecer historico de origem e duplicidade de leads.
- Implementar fluxo administrativo LGPD basico.

## Versao 2

Temas candidatos:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- Relatorios gerenciais e KPIs.
- Parsers dedicados para Webmotors, iCarros e outras origens.
- Configuracao de funil por empresa ou loja.
- Regras avancadas de auditoria e retencao.
- Ferramentas de importacao e exportacao.
- Permissoes avancadas.

## Ideias Futuras

Ideias ainda nao aprovadas para implementacao:

- Aplicativo mobile.
- BI avancado.
- Qualificacao de leads assistida por IA.
- Coaching automatico para vendedores.
- Billing de assinatura.
- Automacao avancada de operacao comercial.
