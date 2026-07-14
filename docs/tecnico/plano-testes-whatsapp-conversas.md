# Plano De Testes: WhatsApp E Conversas Implementados

Este plano cobre o comportamento ja implementado no fluxo de WhatsApp, conversas, envio, leitura, filtros e auditoria.

## Escopo Implementado

- Webhook de WhatsApp para registrar mensagens recebidas.
- Registro de contato, conversa e mensagem.
- Vinculo de conversa a lead quando telefone corresponde.
- Listagem de conversas.
- Escopo por perfil: vendedor, gerente e admin.
- Filtros de conversas por vendedor, status da ultima mensagem e periodo.
- Historico de mensagens da conversa.
- Marcacao de mensagens recebidas como lidas ao abrir historico.
- Envio de template aprovado.
- Envio de texto livre dentro da janela de 24 horas.
- Registro de falhas de envio.
- Eventos de status da WhatsApp Cloud API.
- Auditoria de acesso de gerente/admin a detalhe e historico de conversas.

## Validacoes Automatizadas Atuais

Backend:

- `mvn test`
- Testes de aplicacao em `ConversationServiceTest`.
- Testes de envio por template em `WhatsAppTemplateSenderServiceTest`.
- Testes de envio de texto em `WhatsAppTextSenderServiceTest`.
- Testes de webhook em `WhatsAppWebhookServiceTest` e `WhatsAppWebhookControllerTest`.

Frontend:

- `npm run build`
- `npm run lint`

Observacao:

- O frontend ainda nao possui test runner automatizado de componentes ou E2E configurado.

## Matriz De Testes Funcionais

| Area | Cenario | Resultado esperado | Cobertura atual |
| --- | --- | --- | --- |
| Listagem | Vendedor lista conversas | Retorna apenas conversas sob responsabilidade do vendedor | Unitario backend |
| Listagem | Gerente lista conversas da loja | Retorna conversas do escopo de tenant | Parcial backend |
| Listagem | Admin lista conversas | Retorna todas as conversas | Parcial backend |
| Filtro | Gerente filtra por vendedor | Retorna apenas conversas do vendedor informado dentro do escopo | Unitario backend |
| Filtro | Vendedor informa filtro de outro vendedor | Filtro nao amplia escopo do vendedor | Unitario backend |
| Filtro | Filtrar por status | Retorna conversas cuja ultima mensagem tem o status informado | Unitario backend |
| Filtro | Filtrar por periodo | Retorna conversas pela ultima interacao no intervalo | Unitario backend |
| Historico | Abrir mensagens | Retorna mensagens da conversa em ordem do repositorio | Unitario backend |
| Historico | Abrir mensagens recebidas | Marca `INBOUND RECEIVED` como `READ` | Unitario backend |
| Auditoria | Gerente abre mensagens | Registra acesso em `conversation_access_audits` | Unitario backend |
| Auditoria | Vendedor abre mensagens | Nao registra auditoria gerencial/admin | Unitario backend |
| Envio | Texto livre dentro de 24h | Envia pela API e registra `OUTBOUND TEXT` | Unitario backend |
| Envio | Texto livre fora de 24h | Bloqueia com `WHATSAPP_FREE_TEXT_WINDOW_EXPIRED` | Unitario backend |
| Envio | Template aprovado | Envia e registra mensagem `OUTBOUND TEMPLATE` | Unitario backend |
| Webhook | Mensagem recebida nova | Cria/atualiza contato, conversa e mensagem | Unitario/backend API |
| Webhook | Evento de status | Atualiza mensagem por `externalMessageId` quando encontrada | Unitario backend |

## Casos Manuais Recomendados

### 1. Vendedor Ve Apenas Suas Conversas

Pre-condicoes:

- Usuario vendedor autenticado.
- Pelo menos duas conversas existentes: uma do vendedor e outra de outro vendedor.

Passos:

1. Acessar `/conversations`.
2. Conferir lista retornada.

Resultado esperado:

- A lista mostra apenas conversas em que `responsibleUserId` e o usuario autenticado.
- Filtro por vendedor nao aparece para vendedor.

### 2. Gerente Audita Conversa Da Equipe

Pre-condicoes:

- Usuario gerente autenticado.
- Conversa vinculada a vendedor da mesma loja/escopo.

Passos:

1. Acessar `/conversations`.
2. Filtrar por vendedor.
3. Abrir uma conversa.
4. Abrir o historico de mensagens.
5. Consultar tabela `conversation_access_audits`.

Resultado esperado:

- Conversa aparece para o gerente.
- Historico e carregado.
- Registro de auditoria e criado com `actor_role = MANAGER`.

### 3. Admin Visualiza Conversas Da Operacao

Pre-condicoes:

- Usuario admin autenticado.
- Conversas em mais de uma loja/empresa.

Passos:

1. Acessar `/conversations`.
2. Filtrar por vendedor.
3. Filtrar por periodo.
4. Abrir uma conversa.

Resultado esperado:

- Admin visualiza conversas independente do tenant.
- Filtros reduzem a lista conforme parametros.
- Acesso fica registrado com `actor_role = ADMIN`.

### 4. Filtro Por Status Da Ultima Mensagem

Pre-condicoes:

- Conversas com ultima mensagem em status diferentes, como `RECEIVED`, `READ` e `FAILED`.

Passos:

1. Acessar `/conversations`.
2. Selecionar um status no filtro.

Resultado esperado:

- A lista exibe apenas conversas cuja ultima mensagem possui o status selecionado.

### 5. Janela De Texto Livre Expirada

Pre-condicoes:

- Conversa sem mensagem recebida do cliente nas ultimas 24 horas.
- Lead vinculado e template ativo disponivel.

Passos:

1. Abrir conversa.
2. Tentar enviar texto livre.

Resultado esperado:

- API bloqueia envio com `WHATSAPP_FREE_TEXT_WINDOW_EXPIRED`.
- Tela orienta uso de template aprovado.

## Casos Negativos A Cobrir Em Proximas Iteracoes

- Gerente tentando acessar conversa de outra empresa/loja.
- Usuario sem empresa ou loja tentando listar conversas quando perfil exige tenant.
- `AUDITOR` tentando acessar conversas enquanto regra nao estiver definida.
- Conversa sem vendedor responsavel na listagem de gerente/admin.
- Conversa sem mensagens ao aplicar filtro de status.
- Intervalo de datas invalido, como `startAt` maior que `endAt`.
- Falha de persistencia ao registrar auditoria.
- Falha da WhatsApp Cloud API com payload de erro inesperado.

## Checks De Regressao Antes De Merge

Executar:

```bash
cd backend
mvn test
```

```bash
cd frontend
npm run build
npm run lint
```

Aceite:

- Backend compila e todos os testes passam.
- Frontend compila.
- Lint nao apresenta erros. Warning conhecido atual: `useAuth.tsx` com `react-refresh/only-export-components`.
- Nenhum alerta de vulnerabilidade/CVE deve ser ignorado se aparecer durante build ou auditoria.

## Lacunas De Teste

- Nao ha testes E2E cobrindo login, filtros e abertura de conversa no navegador.
- Nao ha teste de API dedicado para `GET /api/conversations` com query params.
- Nao ha teste de persistencia especifico para a tabela `conversation_access_audits`.
- Nao ha teste frontend de componente para filtros da tela de conversas.
- Nao ha teste de carga/volume para listagem de conversas com muitos registros.
