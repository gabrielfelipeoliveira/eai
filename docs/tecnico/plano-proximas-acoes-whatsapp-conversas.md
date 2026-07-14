# Plano De Proximas Acoes: WhatsApp E Conversas

Este plano organiza proximas acoes tecnicas e de produto para evoluir o fluxo de WhatsApp e conversas sem inventar regras ainda nao aprovadas.

## Objetivos

- Fechar pendencias de produto que bloqueiam regras definitivas.
- Endurecer seguranca, auditoria e rastreabilidade.
- Melhorar a experiencia de gestao de conversas.
- Reduzir divida tecnica nos pontos de maior risco.

## Fase 1: Decisoes De Produto

Prioridade: alta.

- Definir status oficial de conversa ou confirmar que o filtro deve usar status da ultima mensagem.
- Definir comportamento de conversas sem vendedor responsavel para `MANAGER` e `ADMIN`.
- Definir se `AUDITOR` pode visualizar conversas e se o acesso tambem deve ser registrado.
- Definir se `RECEPTIONIST` participa do atendimento por WhatsApp.
- Definir escopo de gerente sem loja vinculada: toda empresa ou acesso bloqueado.
- Definir politica de retencao dos registros de auditoria.
- Definir se auditoria precisa de tela, exportacao ou apenas persistencia para consulta tecnica.

Entrega esperada:

- Atualizar `docs/negocio/business-rules.md`.
- Atualizar `docs/negocio/use-cases.md`.
- Remover ou marcar como resolvidas as pendencias respondidas em `docs/negocio/pendencias.md`.

## Fase 2: Endurecimento De Autorizacao

Prioridade: alta.

- Extrair politica reutilizavel de acesso a conversas para reduzir duplicacao futura.
- Cobrir `GET /api/conversations/{id}` com teste de auditoria para admin e gerente.
- Cobrir cenarios negativos: vendedor tentando abrir conversa de outro vendedor, gerente fora do tenant, usuario sem tenant.
- Avaliar se filtros devem ser aplicados no repositorio em vez de memoria quando volume crescer.
- Padronizar erro para conversa inacessivel sem vazar existencia do recurso quando necessario.

Entrega esperada:

- Testes unitarios e/ou de API para os principais perfis.
- Politica de autorizacao documentada em arquitetura.

## Fase 3: Gestao E Auditoria

Prioridade: media.

- Criar endpoint administrativo para consulta de `conversation_access_audits`, se aprovado pelo produto.
- Adicionar filtros de auditoria por periodo, ator, conversa, vendedor e loja.
- Avaliar exportacao CSV de auditoria usando padrao de relatorios existente.
- Definir mascaramento de dados sensiveis em auditoria e relatorios.
- Adicionar metrica operacional de conversas auditadas por gerente/admin.

Entrega esperada:

- API de consulta de auditoria ou decisao registrada de manter auditoria apenas em banco.
- Testes de persistencia/API quando houver endpoint.

## Fase 4: Experiencia Da Tela De Conversas

Prioridade: media.

- Exibir nome do vendedor responsavel na lista de conversas quando usuario for gerente/admin.
- Adicionar estado de filtros ativos e limpeza mais explicita.
- Validar comportamento responsivo com muitos filtros e conversas.
- Melhorar estados de erro da listagem e do envio de mensagens.
- Avaliar busca textual por cliente, telefone ou conteudo da ultima mensagem, se aprovado.

Entrega esperada:

- Tela de conversas mais adequada para rotina de gestao.
- Build e lint frontend passando.

## Fase 5: WhatsApp Cloud API

Prioridade: media.

- Definir mapeamento oficial de numero WhatsApp para empresa/loja.
- Suportar multiplas contas/numeros quando regra for aprovada.
- Definir comportamento para mensagens recebidas sem lead correspondente.
- Definir tratamento de midias: apenas metadados ou download e armazenamento.
- Revisar validacao de telefone para E.164 ou regra brasileira.

Entrega esperada:

- Documentacao de regra aprovada.
- Migrations novas quando houver novo modelo de conta/canal.
- Testes de webhook e envio cobrindo multi-conta.

## Fase 6: Observabilidade E Operacao

Prioridade: media.

- Adicionar logs estruturados para envio, falha de envio, webhook e auditoria.
- Definir dashboards tecnicos para falhas da WhatsApp Cloud API.
- Monitorar volume de mensagens e crescimento de tabelas.
- Planejar indices adicionais se filtros passarem para consultas em banco.
- Avaliar politica de arquivamento de mensagens antigas.

Entrega esperada:

- Runbook operacional basico.
- Indicadores de saude do fluxo WhatsApp.

## Riscos E Cuidados

- Nao criar status de conversa sem decisao de produto.
- Nao alterar migrations existentes; qualquer schema novo deve usar nova migration.
- Nao colocar regras de perfil em controllers.
- Nao expor entidades JPA diretamente na API.
- Nao assumir regra de multi-conta WhatsApp antes de aprovacao.

## Proxima Acao Recomendada

Comecar pela Fase 1, porque status de conversa, escopo de gerente e papeis `AUDITOR`/`RECEPTIONIST` afetam diretamente API, tela, testes e auditoria.
