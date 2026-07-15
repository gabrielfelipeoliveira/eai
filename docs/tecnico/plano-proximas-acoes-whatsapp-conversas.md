# Plano De Proximas Acoes: WhatsApp E Conversas

Este plano organiza proximas acoes tecnicas para evoluir o fluxo de WhatsApp e conversas com base nas decisoes de produto ja consolidadas.

## Decisoes Ja Consolidadas

- Cada loja tem apenas um numero WhatsApp.
- Numero WhatsApp pertence a loja.
- Conversas pertencem a loja.
- Conversa sem vendedor fica na fila da loja.
- Gerente responde somente se assumir o lead; caso contrario supervisiona.
- Eventos de auditoria ficam registrados tecnicamente no MVP.
- Tela de auditoria fica para fase posterior.
- `AUDITOR` fica fora do MVP.
- Midias de WhatsApp devem ser armazenadas em S3 ou bucket equivalente.
- Dados de status da Meta devem ser salvos.
- Nome do template no EAI deve ser exatamente o aprovado na Meta.
- Placeholders/componentes da Meta devem ser preenchidos automaticamente.
- Telefone deve aceitar formatos suportados pelo WhatsApp/Meta.

## Fase 1: Alinhamento Tecnico

Prioridade: alta.

- Revisar o modelo atual de conversas contra as regras consolidadas.
- Documentar como o numero WhatsApp sera mapeado para empresa/loja quando houver multiplas contas.
- Garantir que politicas de acesso usem papel e tenant de forma reutilizavel.
- Garantir que vendedor nao consiga acessar leads/conversas de outros vendedores.
- Garantir que gerente/admin gere auditoria tecnica ao abrir detalhe/historico quando aplicavel.

Entrega esperada:

- Documentacao tecnica atualizada.
- Testes de autorizacao e auditoria revisados.

## Fase 2: Midias WhatsApp

Prioridade: alta.

- Modelar armazenamento de midias em S3 ou bucket equivalente.
- Persistir metadados e referencia do arquivo armazenado.
- Evitar depender apenas de payload bruto da Meta para recuperar midias.
- Definir tratamento de falhas de download/upload de midia.
- Adicionar testes de webhook para mensagens com midia.

Entrega esperada:

- Plano tecnico de schema/adapters.
- Testes de aplicacao e webhook cobrindo midia.

## Fase 3: Templates E Status Meta

Prioridade: alta.

- Garantir envio usando nome do template exatamente aprovado na Meta.
- Preencher placeholders automaticamente com dados de lead, loja, vendedor e entidades aplicaveis.
- Persistir dados de status recebidos da Meta.
- Testar transicoes de status por `externalMessageId`.

Entrega esperada:

- Testes de envio de template.
- Testes de webhook de status.
- Documentacao de mapeamento de placeholders.

## Fase 4: Experiencia Da Tela De Conversas

Prioridade: media.

- Exibir nome do vendedor responsavel na lista de conversas para gerente/admin.
- Adicionar estado de filtros ativos e limpeza explicita.
- Validar comportamento responsivo com muitos filtros e conversas.
- Melhorar estados de erro da listagem e do envio de mensagens.
- Avaliar busca textual por cliente, telefone ou conteudo da ultima mensagem.

Entrega esperada:

- Tela de conversas mais adequada para rotina de gestao.
- Build e lint frontend passando.

## Fase 5: Observabilidade E Operacao

Prioridade: media.

- Adicionar logs estruturados para envio, falha de envio, webhook, midia e auditoria.
- Definir dashboards tecnicos para falhas da WhatsApp Cloud API.
- Monitorar volume de mensagens, eventos e midias.
- Planejar indices adicionais se filtros passarem para consultas em banco.
- Avaliar politica de arquivamento de mensagens WhatsApp.

## Fora Do MVP

- Tela de auditoria.
- Escopo operacional de `AUDITOR`.
- Distribuicao automatica.
- SLA, follow-ups e notificacoes.
- Relatorios gerenciais completos.
