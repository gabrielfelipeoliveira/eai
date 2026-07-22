# Importador De Leads Por E-Mail

O EAI possui uma primeira base para entrada automatica de leads por contas de e-mail IMAP.

## Configuracao

As contas de e-mail sao gerenciadas no frontend em `E-mails` e pelos endpoints do backend:

- `GET /api/email-accounts`
- `GET /api/email-accounts/{id}`
- `POST /api/email-accounts`
- `PUT /api/email-accounts/{id}`
- `DELETE /api/email-accounts/{id}`
- `POST /api/email-accounts/{id}/test`
- `POST /api/email-accounts/{id}/sync`

Cada conta pertence ao escopo de uma empresa e loja e armazena:

- nome
- host
- porta
- usuario
- senha criptografada
- protocolo `IMAP`
- flag de SSL
- flag ativo

Configuracoes IMAP comuns:

- Gmail: `imap.gmail.com`, porta `993`, SSL habilitado, senha de aplicativo obrigatoria.
- Outlook/Microsoft 365: `outlook.office365.com`, porta `993`, SSL habilitado.
- Provedores customizados: use host, porta, usuario e senha fornecidos pelo provedor.

Use `POST /api/email-accounts/{id}/test` depois de criar a conta para validar credenciais e conectividade.
Use `POST /api/email-accounts/{id}/sync` para importar manualmente mensagens nao lidas.

Contas de e-mail podem ser gerenciadas por `ADMIN` e gerente geral.

## Scheduler

O job automatico fica desabilitado por padrao:

```yaml
eai:
  email:
    importer:
      enabled: false
      fixed-delay: 60000
```

Producao pode habilita-lo com:

```text
EAI_EMAIL_IMPORTER_ENABLED=true
EAI_EMAIL_IMPORTER_FIXED_DELAY=60000
```

Quando habilitado, o scheduler importa contas ativas a cada intervalo configurado.

## Parsing

O parser atual e generico. Ele tenta extrair dados do lead. Para o MVP, devem ser preservados apenas os dados do lead extraidos do e-mail, nao o e-mail original completo.

Ele tenta extrair:

- nome do cliente
- telefone
- e-mail
- veiculo de interesse
- mensagem original
- origem

Ele reconhece rotulos comuns como `Nome`, `Telefone`, `E-mail`, `Veiculo` e `Origem`, e tambem usa regex como fallback para extrair telefone e e-mail.

A camada de aplicacao usa a interface `EmailParser` e uma implementacao generica. Parsers especificos por provedor ou marketplace podem ser adicionados depois implementando `EmailParser` e dando prioridade maior a implementacao.

## Regra De Duplicidade

Durante a importacao, se existir um lead com o mesmo telefone/WhatsApp na mesma loja, o lead importado deve indicar duplicidade.

Se nenhuma duplicidade for encontrada, o lead importado e criado com origem `EMAIL` e status `NEW`.

Cada chegada deve ficar registrada no historico.
Entrada duplicada referencia o lead anterior por `related_lead_id` e gera novo lead marcado como `DUPLICATED`.
Entradas de historico criadas pelo scheduler usam usuario nulo quando nao houver usuario operacional associado.

Mensagens importadas pelo IMAP sao marcadas como lidas na conta original apos o sucesso transacional da importacao.
Cada execucao salva um registro em `email_import_history` com status, quantidade de mensagens lidas, leads criados, duplicados marcados, mensagem de resultado e timestamps de inicio/fim.
Importacoes com falha retornam status `FAILED`, atualizam o ultimo status da conta e registram a falha em `email_import_history`, permitindo nova tentativa posterior.
Testes de conexao com falha ainda propagam o erro para a API apos atualizar o ultimo status da conta; notificacao explicita de administradores depende de infraestrutura futura.

Excluir uma conta de e-mail preserva o historico de importacao; nesse caso o identificador da conta pode ficar nulo no historico fisico.

## Seguranca De Senhas

Senhas nunca sao retornadas pela API e nao sao armazenadas como texto puro.

A implementacao atual usa a interface `EncryptionService` com AES/GCM e payload versionado `v1`. A chave efetiva e derivada de `eai.email.credentials.secret`, configurada por `EAI_EMAIL_CREDENTIALS_SECRET`; o valor padrao existe apenas para desenvolvimento local e nao deve ser usado em producao.

Valores antigos gravados como Base64 simples continuam sendo lidos para compatibilidade. Ao atualizar a senha de uma conta IMAP, o novo valor passa a ser salvo no formato criptografado versionado.

Para producao, o segredo deve vir de um gerenciador externo ou variavel protegida fora do banco.

## Politica De Rotacao E Migracao De Credenciais

Estado atual:

- Novas credenciais IMAP sao gravadas com AES/GCM em payload versionado `v1`.
- Credenciais legadas em Base64 simples continuam sendo lidas apenas para compatibilidade.
- Atualizar manualmente a senha de uma conta IMAP regrava a credencial no formato `v1`.
- A implementacao atual usa uma unica chave efetiva derivada de `EAI_EMAIL_CREDENTIALS_SECRET`.

Politica operacional vigente:

- `EAI_EMAIL_CREDENTIALS_SECRET` deve ser tratado como segredo critico de producao.
- O segredo nao deve ser trocado diretamente em producao sem uma janela operacional planejada, porque credenciais `v1` gravadas com a chave anterior deixarao de ser descriptografadas pela implementacao atual.
- Rotacao emergencial por suspeita de vazamento deve considerar indisponibilidade temporaria da importacao IMAP ate que as contas sejam regravadas com o novo segredo.
- A rotacao planejada deve ser feita somente apos existir suporte tecnico para descriptografar com chave anterior e criptografar com chave atual, ou apos uma migracao manual controlada de todas as senhas IMAP.

Migracao de credenciais legadas:

- A estrategia segura preferencial e migrar de forma online: ler a credencial legada ou cifrada antiga, validar a conexao IMAP e regravar a senha no formato `v1` com a chave atual dentro de transacao.
- A migracao em lote deve registrar quantidade de contas avaliadas, migradas, ignoradas e com falha, sem registrar senhas ou payloads sensiveis.
- Falhas de migracao devem preservar o valor original para permitir nova tentativa.
- Enquanto nao houver job/script aprovado, a migracao acontece apenas quando um administrador atualiza a senha da conta.

Proximo card tecnico:

- `EAI-036`: implementar suporte a `EAI_EMAIL_CREDENTIALS_PREVIOUS_SECRETS` ou mecanismo equivalente de keyring.
- Adicionar job/script administrativo idempotente para recriptografar credenciais IMAP legadas e credenciais cifradas com chave anterior.
- Testar rollback, falha parcial e ausencia de vazamento de segredo em logs.

## Limitacoes

- Apenas IMAP e suportado.
- O parser generico pode nao reconhecer templates fora do padrao.
- Mensagens sao lidas de `INBOX`.
- O leitor deve marcar mensagens importadas como lidas na conta original; se a implementacao atual ainda nao fizer isso, tratar como divergencia tecnica a corrigir.
- Anexos sao ignorados.
- E-mails somente em HTML podem ter extracao limitada ate que um parser HTML-para-texto seja adicionado.
