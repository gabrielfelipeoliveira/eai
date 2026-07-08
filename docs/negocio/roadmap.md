# Roadmap

Este roadmap organiza a evolucao conhecida do projeto. Ele nao e um compromisso de implementar comportamento de negocio sem aprovacao do Product Owner. Duvidas de priorizacao e escopo ficam centralizadas em [Pendencias de produto](pendencias.md).

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

## Sprint 2: Persistencia De Conversas E Mensagens

Objetivo:

- Criar a estrutura de dados para armazenar conversas e mensagens do WhatsApp dentro da plataforma.

Escopo entregue:

- Contatos de WhatsApp por telefone e loja.
- Conversas vinculadas a contato, lead e vendedor responsavel quando houver lead correspondente.
- Mensagens de conversa com direcao, tipo, status, conteudo e metadados basicos.
- Persistencia de mensagens recebidas pelo webhook da Meta.
- Registro de mensagem de saida no fluxo existente de geracao de link do WhatsApp.
- Consulta autenticada de conversas e mensagens pelo backend.

Pendencias:

- Regras oficiais de roteamento multi-conta WhatsApp por empresa/loja.
- Criacao automatica de lead para mensagem sem lead correspondente.
- Tratamento de eventos de status da Meta.
- Armazenamento/download de midias.

## Sprint 3: Envio De Template WhatsApp

Objetivo:

- Permitir envio de templates aprovados pela WhatsApp Cloud API para iniciar ou retomar conversas com leads.

Escopo entregue:

- Servico de envio de template pela WhatsApp Cloud API.
- Endpoint autenticado para disparar template para um lead.
- Validacao basica de telefone do lead antes do envio.
- Registro da mensagem enviada no historico da conversa.
- Registro de falhas de envio como mensagem `FAILED`.
- Vinculo do retorno bruto da API do WhatsApp a mensagem enviada.
- Atualizacao do status inicial da mensagem para `SENT` ou `FAILED`.

Pendencias:

- Regras oficiais de idioma padrao por loja/template.
- Mapeamento oficial entre placeholders do EAI e variaveis aprovadas no template da Meta.
- Suporte a componentes de header, botoes e midia em templates.

## Sprint 4: Listagem De Conversas

Objetivo:

- Permitir que o vendedor visualize suas conversas do WhatsApp dentro da plataforma EAI.

Escopo entregue:

- Endpoint autenticado de listagem de conversas.
- Filtro para vendedor visualizar apenas conversas sob sua responsabilidade.
- Ordenacao pela ultima interacao registrada.
- Resumo de lead ou contato, telefone, ultima mensagem, horario da ultima interacao e quantidade de mensagens nao lidas.
- Tela inicial de listagem de chats no frontend.

Pendencias:

- Fluxo oficial para marcar mensagens como lidas.
- Regras oficiais para usuarios visualizarem conversas sem vendedor responsavel.

## Versao 1

Temas candidatos:

- Endurecer permissoes e escopo de tenant.
- Melhorar regras do ciclo de vida do lead.
- Melhorar workflow de follow-up.
- Melhorar dashboard e indicadores de performance comercial.
- Melhorar parsers de importacao por e-mail.
- Melhorar modularidade e usabilidade do frontend.
- Adicionar seguranca e observabilidade de producao.

## Versao 2

Temas candidatos:

- Relatorios avancados.
- Workflows de notificacao.
- Trilhas de auditoria.
- Ferramentas de importacao e exportacao.
- Permissoes avancadas.
- Novas origens de lead e parsers.
- Automacao de deploy em producao.

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
