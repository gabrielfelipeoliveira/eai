# Registro Do Teste Real: WhatsApp E Conversas

Este documento registra o passo a passo executado para validar, de ponta a ponta, o recebimento de uma mensagem real do WhatsApp Cloud API em ambiente local. O teste foi concluido com sucesso: a mensagem enviada por um telefone real chegou ao webhook, foi persistida no banco e a conversa apareceu para o usuario logado no frontend.

Nao contem tokens, secrets ou IDs reais. Onde necessario, use os valores obtidos no seu proprio painel da Meta.

## Pre-Requisitos Que Ja Estavam Prontos

- Backend rodando localmente (`mvn spring-boot:run`, porta 8080).
- Frontend rodando localmente (`npm run dev`, porta 5173).
- PostgreSQL local com empresa, loja e usuarios seed cadastrados.

## Passo 1: Criar Conta E App No Meta For Developers

1. Criar conta em `developers.facebook.com`.
2. Em **Meus Apps**, clicar em **Criar app**.
3. Tipo de app: **Empresa (Business)**.
4. Dar um nome ao app (ex: `eai-wpp-dev`).

## Passo 2: Adicionar O Produto WhatsApp

1. No painel do app, adicionar o produto **WhatsApp**.
2. A Meta cria automaticamente um numero de teste e uma WhatsApp Business Account (WABA).
3. Na tela **WhatsApp > API Setup**, coletar:
   - `Phone number ID`
   - `WhatsApp Business Account ID`
   - `Temporary access token` (gerado pelo botao "Generate token", valido por 24h)
4. Em **Configuracoes do App > Basico**, coletar a **Chave secreta do app** (`App Secret`).
5. Definir um `Verify token` proprio, qualquer string (ex: `eai-local-verify-123`).

## Passo 3: Cadastrar Telefone De Teste

1. Ainda em **API Setup**, secao **"To"**, clicar em **Manage phone number list**.
2. Adicionar o telefone pessoal que vai enviar a mensagem de teste e confirmar o codigo recebido.

## Passo 4: Buscar UUIDs Locais De Empresa E Loja

As variaveis `META_WHATSAPP_COMPANY_ID` e `META_WHATSAPP_STORE_ID` precisam apontar para registros existentes no banco local:

```sql
SELECT id, name FROM companies;
SELECT id, name, company_id FROM stores;
```

## Passo 5: Configurar Variaveis De Ambiente E Reiniciar O Backend

No terminal onde o backend sera iniciado (Git Bash, exemplo):

```bash
export JWT_SECRET="eai-local-development-secret-change-me"
export META_WHATSAPP_PHONE_NUMBER_ID="<phone-number-id>"
export META_WHATSAPP_BUSINESS_ACCOUNT_ID="<waba-id>"
export META_WHATSAPP_ACCESS_TOKEN="<access-token>"
export META_WHATSAPP_APP_SECRET="<app-secret>"
export META_WHATSAPP_VERIFY_TOKEN="eai-local-verify-123"
export META_WHATSAPP_GRAPH_API_VERSION="v25.0"
export META_WHATSAPP_COMPANY_ID="<uuid-da-empresa>"
export META_WHATSAPP_STORE_ID="<uuid-da-loja>"

cd backend
mvn spring-boot:run
```

Se o backend ja estava rodando sem essas variaveis, e necessario encerrar o processo anterior e subir de novo nesta sessao com as variaveis exportadas, senao o Spring nao le os novos valores.

## Passo 6: Expor O Backend Local Publicamente

Usado `localtunnel`, que nao exige cadastro (alternativa ao `ngrok`):

```bash
npx localtunnel --port 8080
```

Isso gera uma URL publica do tipo `https://<nome-aleatorio>.loca.lt`, que muda a cada reinicio do comando.

Validar que o tunel responde antes de configurar na Meta:

```bash
curl -s "https://<seu-subdominio>.loca.lt/api/webhooks/whatsapp?hub.mode=subscribe&hub.verify_token=eai-local-verify-123&hub.challenge=teste123"
```

Resposta esperada: `teste123` (sem pagina de interstitial do localtunnel).

## Passo 7: Configurar O Webhook No Painel Da Meta

1. Em **WhatsApp > Configuration**, secao **Webhook**, clicar em **Edit**.
2. **Callback URL**: `https://<seu-subdominio>.loca.lt/api/webhooks/whatsapp`
3. **Verify token**: o mesmo valor de `META_WHATSAPP_VERIFY_TOKEN`.
4. Clicar em **Verify and save**.
5. Na lista de **Webhook fields**, clicar em **Manage** e marcar o campo **`messages`**.

## Passo 8 (Armadilha Encontrada): Inscrever O App Na WABA

Mesmo com o webhook verificado e o campo `messages` marcado, a mensagem real nao chegou ao backend na primeira tentativa. O diagnostico via Graph API mostrou que a WABA estava inscrita no app interno de testes da propria Meta (`WA DevX Webhook Events 1P App`), e nao no app criado pelo usuario:

```bash
curl -s "https://graph.facebook.com/v25.0/<waba-id>/subscribed_apps?access_token=<access-token>"
```

Resposta mostrando apenas o app da Meta:

```json
{"data":[{"whatsapp_business_api_data":{"name":"WA DevX Webhook Events 1P App", "id":"..."}}]}
```

Correcao: inscrever explicitamente o app do usuario na WABA via `POST`:

```bash
curl -s -X POST "https://graph.facebook.com/v25.0/<waba-id>/subscribed_apps?access_token=<access-token>"
```

Resposta esperada: `{"success":true}`. Uma nova consulta `GET` no mesmo endpoint deve mostrar o app proprio (ex: `eai-wpp-dev`) na lista, junto com o app da Meta.

Sem esse passo, o callback URL valida corretamente na tela da Meta, mas nenhum evento de mensagem chega ao webhook.

## Passo 9: Enviar Mensagem De Teste E Validar

1. Do telefone cadastrado como testador, enviar uma mensagem de texto para o numero de teste do WhatsApp.
2. Acompanhar o log do backend. Log de sucesso:

```text
WhatsApp webhook persisted 1 incoming message(s) and processed 0 status update(s)
```

3. Confirmar no banco:

```sql
SELECT id, phone, display_name, created_at FROM whatsapp_contacts ORDER BY created_at DESC;
SELECT id, conversation_id, direction, type, status, content, created_at FROM conversation_messages ORDER BY created_at DESC;
```

## Passo 10: Validar No Frontend

1. Acessar `http://127.0.0.1:5173`.
2. Fazer login com um usuario vendedor, gerente ou admin.
3. Abrir `/conversations`.
4. Confirmar que a conversa recebida aparece na lista.

Resultado obtido: conversa apareceu corretamente para o usuario logado.

## Resumo Das Licoes Aprendidas

- Verificar o webhook (`Verify and save`) nao garante que o app recebera eventos: e preciso tambem assinar o campo `messages` em **Webhook fields**.
- Assinar o campo `messages` tambem nao garante recebimento: a WABA precisa estar inscrita (`subscribed_apps`) no app correto. Por padrao, apos criar o app pelo fluxo guiado, a WABA pode ficar inscrita apenas no app interno de testes da Meta.
- Use `GET /{waba-id}/subscribed_apps` para diagnosticar rapidamente qual app esta recebendo os eventos, antes de suspeitar do tunel ou do codigo do backend.
- `localtunnel` e uma alternativa valida ao `ngrok` para teste local sem exigir cadastro, mas a URL muda a cada reinicio do processo, exigindo reconfigurar o callback na Meta quando isso acontecer.
- Tokens, app secret e IDs reais nunca devem ser colados em chats, commits ou documentos versionados. Depois de concluir os testes, revogar/regerar o token temporario e o app secret no painel da Meta.

## Limpeza Apos O Teste

- Revogar ou regenerar o access token temporario da Meta.
- Encerrar o processo do `localtunnel`.
- Apagar mensagens/conversas de teste apenas no banco local, se necessario.
