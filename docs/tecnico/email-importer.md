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

O parser atual e generico. Ele tenta extrair:

- nome do cliente
- telefone
- e-mail
- veiculo de interesse
- mensagem original
- origem

Ele reconhece rotulos comuns como `Nome`, `Telefone`, `E-mail`, `Veiculo` e `Origem`, e tambem usa regex como fallback para extrair telefone e e-mail.

A camada de aplicacao usa a interface `EmailParser` e uma implementacao generica. Parsers especificos por provedor ou marketplace podem ser adicionados depois implementando `EmailParser` e dando prioridade maior a implementacao.

## Regra De Duplicidade

Durante a importacao, se existir um lead com o mesmo telefone normalizado e o mesmo veiculo na mesma loja nos ultimos 7 dias, o lead importado e criado com status `DUPLICATED`.

Se nenhuma duplicidade for encontrada, o lead importado e criado com origem `EMAIL` e status `NEW`.

Ambos os caminhos preservam o corpo original do e-mail em `originalMessage` e registram historico do lead. Entradas de historico criadas pelo scheduler usam um registro de sistema sem usuario.

## Seguranca De Senhas

Senhas nunca sao retornadas pela API e nao sao armazenadas como texto puro.

A implementacao atual usa a interface `EncryptionService` com uma implementacao de desenvolvimento baseada em Base64. Isso e apenas uma obfuscacao temporaria para desenvolvimento local e deve ser substituido em producao por criptografia real baseada em chave gerenciada, como KMS, Vault ou segredo de aplicacao armazenado fora do banco.

## Limitacoes

- Apenas IMAP e suportado.
- O parser generico pode nao reconhecer templates fora do padrao.
- Mensagens sao lidas de `INBOX`.
- O leitor atual busca mensagens nao lidas e mensagens recebidas depois de `lastReadAt`, mas nao marca mensagens como lidas.
- Anexos sao ignorados.
- E-mails somente em HTML podem ter extracao limitada ate que um parser HTML-para-texto seja adicionado.
