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

Cada chegada deve ficar registrada no historico. Entrada duplicada fica na mesma conversa anterior, mas gera novo lead marcado como duplicado. Entradas de historico criadas pelo scheduler usam um registro de sistema sem usuario quando aplicavel.

Mensagens importadas devem ser marcadas como lidas na conta original. Importacoes com falha devem ser tentadas novamente e testes com falha devem notificar administradores.

Excluir uma conta de e-mail deve preservar o historico de importacao.

## Seguranca De Senhas

Senhas nunca sao retornadas pela API e nao sao armazenadas como texto puro.

A implementacao atual usa a interface `EncryptionService` com uma implementacao de desenvolvimento baseada em Base64. Isso e apenas uma obfuscacao temporaria para desenvolvimento local e deve ser substituido em producao por criptografia real baseada em chave gerenciada, como KMS, Vault ou segredo de aplicacao armazenado fora do banco.

## Limitacoes

- Apenas IMAP e suportado.
- O parser generico pode nao reconhecer templates fora do padrao.
- Mensagens sao lidas de `INBOX`.
- O leitor deve marcar mensagens importadas como lidas na conta original; se a implementacao atual ainda nao fizer isso, tratar como divergencia tecnica a corrigir.
- Anexos sao ignorados.
- E-mails somente em HTML podem ter extracao limitada ate que um parser HTML-para-texto seja adicionado.
