# Plano De Teste Local Real: WhatsApp E Conversas

Este roteiro descreve como testar localmente uma conversa real usando WhatsApp Cloud API, backend local, frontend local e um tunel publico para o webhook.

Nao registre tokens reais, telefone de cliente real, app secret ou URLs privadas em commits, prints ou issues publicas.

## Objetivo

Validar, com uma mensagem real de WhatsApp, que o EAI consegue:

- Receber webhook da Meta.
- Criar ou atualizar contato WhatsApp.
- Criar ou atualizar conversa.
- Registrar mensagem recebida.
- Exibir conversa no frontend.
- Marcar mensagens recebidas como lidas ao abrir historico.
- Enviar resposta quando a janela de 24 horas estiver aberta.
- Registrar acesso de gerente/admin quando abrir conversa.

## Pre-Requisitos

- Conta Meta for Developers com WhatsApp Cloud API configurada.
- Numero de teste ou numero real aprovado pela Meta.
- Telefone pessoal cadastrado como destinatario de teste, quando usando ambiente de desenvolvimento da Meta.
- `ngrok`, Cloudflare Tunnel ou ferramenta equivalente para expor `localhost:8080`.
- Java 21.
- Node.js e npm.
- Docker para PostgreSQL local, se o banco ainda nao estiver rodando.
- Branch atualizada com as migrations incluindo `V5__conversation_access_audits.sql`.

## Variaveis De Ambiente Necessarias

Configure no terminal em que o backend sera iniciado:

```powershell
$env:JWT_SECRET="eai-local-development-secret-change-me"
$env:META_WHATSAPP_PHONE_NUMBER_ID="phone-number-id-da-meta"
$env:META_WHATSAPP_BUSINESS_ACCOUNT_ID="business-account-id-da-meta"
$env:META_WHATSAPP_ACCESS_TOKEN="token-da-meta"
$env:META_WHATSAPP_APP_SECRET="app-secret-da-meta"
$env:META_WHATSAPP_VERIFY_TOKEN="um-token-local-de-verificacao"
$env:META_WHATSAPP_GRAPH_API_VERSION="v25.0"
$env:META_WHATSAPP_COMPANY_ID="uuid-da-empresa-local"
$env:META_WHATSAPP_STORE_ID="uuid-da-loja-local"
```

Observacoes:

- `META_WHATSAPP_COMPANY_ID` e `META_WHATSAPP_STORE_ID` precisam existir no banco local.
- O webhook atual usa essas duas variaveis para resolver tenant enquanto o mapeamento oficial por numero/conta nao estiver definido.
- O token da Meta precisa ter permissao para envio de mensagens pelo numero configurado.

## Preparar Banco Local

1. Subir PostgreSQL:

```powershell
docker compose up -d
```

2. Iniciar backend uma vez para aplicar migrations:

```powershell
cd backend
mvn spring-boot:run
```

3. Confirmar que existem empresa, loja, usuarios e pelo menos um vendedor ativo.

Se usar dados seed locais, pegue os UUIDs diretamente no banco:

```sql
SELECT id, name FROM companies;
SELECT id, name, company_id FROM stores;
SELECT id, name, email, company_id, store_id FROM users;
```

## Subir Backend E Frontend

Backend:

```powershell
cd backend
mvn spring-boot:run
```

Frontend:

```powershell
cd frontend
npm run dev -- --host 127.0.0.1 --port 5173
```

URLs locais esperadas:

- Backend: `http://localhost:8080`
- Frontend: `http://127.0.0.1:5173`
- Swagger local, se habilitado: `http://localhost:8080/swagger-ui.html`

## Expor Webhook Local

Com ngrok:

```powershell
ngrok http 8080
```

Copie a URL HTTPS gerada, por exemplo:

```text
https://abc123.ngrok-free.app
```

Webhook callback URL na Meta:

```text
https://abc123.ngrok-free.app/api/webhooks/whatsapp
```

Verify token na Meta:

```text
mesmo valor de META_WHATSAPP_VERIFY_TOKEN
```

Campos de webhook para assinar:

- `messages`

Resultado esperado:

- A verificacao do webhook na Meta retorna sucesso.
- O backend recebe `GET /api/webhooks/whatsapp` e responde o challenge.

## Cenario 1: Receber Uma Mensagem Real

Passos:

1. No telefone cadastrado para teste, envie uma mensagem para o numero WhatsApp Cloud API.
2. Aguarde a Meta chamar o webhook local.
3. Verifique logs do backend.
4. Acesse o frontend em `/conversations`.
5. Faça login com vendedor, gerente ou admin.
6. Confira se a conversa apareceu.

Resultado esperado:

- Uma linha em `whatsapp_contacts` para o telefone.
- Uma linha em `conversations`.
- Uma linha em `conversation_messages` com:
  - `direction = INBOUND`
  - `type = TEXT`, quando mensagem for texto
  - `status = RECEIVED`
- A tela de conversas mostra o contato ou lead correspondente.
- Contador de nao lidas aumenta.

Consultas uteis:

```sql
SELECT id, phone, display_name, lead_id, created_at, updated_at
FROM whatsapp_contacts
ORDER BY created_at DESC;

SELECT id, company_id, store_id, contact_id, lead_id, responsible_user_id, created_at, updated_at
FROM conversations
ORDER BY updated_at DESC;

SELECT id, conversation_id, direction, type, status, content, created_at
FROM conversation_messages
ORDER BY created_at DESC;
```

## Cenario 2: Vincular Conversa A Lead Existente

Pre-condicao:

- Criar ou identificar um lead local com telefone igual ao telefone que enviara a mensagem.
- O lead deve estar na mesma loja configurada em `META_WHATSAPP_STORE_ID`.

Passos:

1. Envie nova mensagem do WhatsApp.
2. Confira a conversa criada/atualizada.

Resultado esperado:

- `whatsapp_contacts.lead_id` preenchido.
- `conversations.lead_id` preenchido.
- `conversations.responsible_user_id` igual ao vendedor responsavel pelo lead, quando o lead tiver vendedor atribuido.
- Na UI, a conversa aparece para o vendedor responsavel.

## Cenario 3: Abrir Historico E Marcar Como Lida

Passos:

1. Faça login como vendedor responsavel.
2. Abra `/conversations`.
3. Clique na conversa recebida.
4. Consulte a mensagem no banco.

Resultado esperado:

- Mensagens `INBOUND` com `status = RECEIVED` da conversa passam para `READ`.
- O contador de nao lidas na UI diminui.

Consulta:

```sql
SELECT direction, status, content, created_at, updated_at
FROM conversation_messages
WHERE conversation_id = 'uuid-da-conversa'
ORDER BY created_at ASC;
```

## Cenario 4: Auditar Acesso De Gerente Ou Admin

Passos:

1. Faça login como `MANAGER` ou `ADMIN`.
2. Abra `/conversations`.
3. Use filtros de vendedor, status ou periodo se quiser.
4. Abra a conversa.
5. Consulte auditoria.

Resultado esperado:

- Uma linha em `conversation_access_audits` com:
  - `conversation_id` da conversa aberta.
  - `actor_user_id` do gerente/admin.
  - `actor_role = MANAGER` ou `ADMIN`.
  - `access_type = LIST_MESSAGES`, quando abrir mensagens.

Consulta:

```sql
SELECT conversation_id, actor_user_id, actor_role, access_type, accessed_at
FROM conversation_access_audits
ORDER BY accessed_at DESC;
```

## Cenario 5: Enviar Texto Livre Dentro Da Janela De 24 Horas

Pre-condicao:

- A conversa precisa ter mensagem recebida do cliente nas ultimas 24 horas.

Passos:

1. Abra a conversa no frontend.
2. Digite uma resposta.
3. Clique em enviar.
4. Confira o telefone do cliente.
5. Confira banco e logs.

Resultado esperado:

- Cliente recebe mensagem no WhatsApp.
- Nova linha em `conversation_messages` com:
  - `direction = OUTBOUND`
  - `type = TEXT`
  - `status = SENT`, se a Meta aceitou o envio
  - `external_message_id` preenchido quando retornado pela Meta
  - `raw_payload` com resposta bruta do provedor

## Cenario 6: Receber Evento De Status

Depois do envio de texto ou template, a Meta deve chamar o webhook com status.

Resultado esperado:

- `conversation_message_events` recebe uma linha para cada status.
- A mensagem enviada pode evoluir para `DELIVERED`, `READ` ou `FAILED`.

Consulta:

```sql
SELECT external_message_id, status, failure_reason, occurred_at, created_at
FROM conversation_message_events
ORDER BY created_at DESC;
```

## Cenario 7: Enviar Template

Pre-condicoes:

- Template aprovado na Meta.
- Template ativo cadastrado no EAI para a mesma loja do lead.
- Nome do template no EAI igual ao nome aprovado na Meta.
- Lead com telefone valido para WhatsApp.

Passos:

1. Acesse um lead com conversa vinculada ou use a acao de envio de template existente.
2. Se a janela de 24 horas estiver expirada, tente responder pela conversa para exibir a orientacao de template.
3. Selecione template aprovado.
4. Envie.

Resultado esperado:

- Cliente recebe template.
- `lead_communications` recebe registro legado da comunicacao.
- `conversation_messages` recebe mensagem `OUTBOUND TEMPLATE`.
- Falha de envio fica registrada como `FAILED`.

## Criterios De Aceite Do Teste Real

O teste local real e considerado aprovado quando:

- Webhook da Meta valida com sucesso.
- Mensagem real recebida cria conversa e mensagem no banco local.
- Conversa aparece na tela `/conversations`.
- Vendedor enxerga apenas conversa sob sua responsabilidade.
- Gerente/admin consegue abrir conversa e gera auditoria.
- Mensagens recebidas mudam de `RECEIVED` para `READ` ao abrir historico.
- Envio de texto dentro de 24 horas chega no WhatsApp real.
- Evento de status da Meta e persistido.

## Troubleshooting

### Webhook nao valida na Meta

- Conferir se ngrok esta rodando.
- Conferir se callback termina com `/api/webhooks/whatsapp`.
- Conferir se `META_WHATSAPP_VERIFY_TOKEN` e igual ao token informado na Meta.
- Conferir logs do backend durante a verificacao.

### Mensagem nao chega ao backend

- Conferir assinatura do campo `messages` na Meta.
- Conferir se o app esta em modo desenvolvimento e se o telefone remetente esta cadastrado como testador.
- Conferir se a URL do ngrok mudou e foi atualizada na Meta.

### Conversa nao aparece para vendedor

- Conferir se o lead foi encontrado por telefone.
- Conferir `conversations.responsible_user_id`.
- Conferir se o usuario logado e o vendedor responsavel.
- Testar como gerente/admin para separar problema de permissao e problema de criacao da conversa.

### Envio falha

- Conferir `META_WHATSAPP_PHONE_NUMBER_ID`.
- Conferir `META_WHATSAPP_ACCESS_TOKEN`.
- Conferir validade/permissoes do token.
- Conferir se a conversa tem mensagem recebida nas ultimas 24 horas para texto livre.
- Para template, conferir se o template esta aprovado e se o nome cadastrado no EAI bate com o nome da Meta.

### Auditoria nao aparece

- Conferir se o usuario logado e `MANAGER` ou `ADMIN`.
- Abrir mensagens pela rota/tela de historico, pois a auditoria e gravada ao consultar detalhe ou mensagens.
- Conferir se a migration `V5__conversation_access_audits.sql` foi aplicada.

## Evidencias Para Registrar

- Horario do teste.
- Usuario usado no login.
- Perfil usado no login.
- UUID da conversa.
- UUID da mensagem recebida.
- UUID da mensagem enviada, quando houver.
- Linha de auditoria criada para gerente/admin.
- Prints da tela de conversas sem expor telefone/token real quando forem compartilhados.

## Limpeza Depois Do Teste

Se os dados forem apenas de experimento local:

- Apagar mensagens/conversas de teste apenas no banco local.
- Nao alterar migrations.
- Nao commitar tokens, logs com payload sensivel ou prints com telefone real.
