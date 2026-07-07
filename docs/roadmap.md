# Roadmap

Este roadmap organiza a evolucao do projeto. Ele nao e um compromisso de implementar comportamento de negocio sem aprovacao do Product Owner.

## Sprint 0: Base de Engenharia

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
- Mudancas de regra de negocio.
- Migrations de banco.
- Mudancas de API.
- Grandes refactors.

Definicao de pronto:

- Estrutura documental obrigatoria existe.
- Perguntas pendentes para o Product Owner estao explicitas.
- Decisoes arquiteturais e decisoes futuras estao documentadas.
- Instrucoes existentes de setup continuam acessiveis.

## MVP

Escopo candidato com base na implementacao e documentacao atuais:

- Autenticacao e autorizacao.
- Gestao de empresas e lojas.
- Gestao de usuarios e vendedores.
- Cadastro e listagem de leads.
- Atribuicao de leads.
- Gestao de status do funil.
- Notas, tags e historico de lead.
- Visao basica de pipeline.
- Agenda de follow-up.
- Base de distribuicao e SLA.
- Templates de mensagem.
- Geracao de link de WhatsApp.
- Base do importador de leads por e-mail.
- Dashboard basico.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais modulos candidatos sao obrigatorios no MVP?
- Qual e a menor entrega que gera valor para uma concessionaria?
- Quais papeis de usuario devem existir no primeiro dia?
- Quais relatorios operacionais sao obrigatorios no MVP?

## Versao 1

Temas candidatos:

- Endurecer permissoes e escopo de tenant.
- Melhorar regras do ciclo de vida do lead.
- Melhorar workflow de follow-up.
- Melhorar dashboard e indicadores de performance comercial.
- Melhorar parsers de importacao por e-mail.
- Melhorar modularidade e usabilidade do frontend.
- Adicionar seguranca e observabilidade de producao.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais workflows precisam estar completos antes de um piloto pago?
- Quais integracoes sao obrigatorias para as lojas na primeira versao?

## Versao 2

Temas candidatos:

- Relatorios avancados.
- Workflows de notificacao.
- Trilhas de auditoria.
- Ferramentas de importacao e exportacao.
- Permissoes avancadas.
- Novas origens de lead e parsers.
- Automacao de deploy em producao.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais workflows avancados tem prioridade comercial?
- Quais sistemas externos precisam ser integrados?

## Ideias Futuras

Ideias ainda nao aprovadas para implementacao:

- Integracao nativa com WhatsApp Business API.
- Caixa de entrada de conversas de mao dupla.
- Adapters de importacao por marketplace.
- Aplicativo mobile.
- BI avancado.
- Qualificacao de leads assistida por IA.
- Coaching automatico para vendedores.
- Billing de assinatura.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais ideias futuras sao apostas estrategicas?
- Quais ideias devem ser explicitamente excluidas?
