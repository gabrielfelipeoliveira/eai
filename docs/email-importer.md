# Email Lead Importer

EAI supports a first foundation for automatic lead intake through IMAP e-mail accounts.

## Configuration

E-mail accounts are managed in the frontend at `E-mails` and through the backend endpoints:

- `GET /api/email-accounts`
- `GET /api/email-accounts/{id}`
- `POST /api/email-accounts`
- `PUT /api/email-accounts/{id}`
- `DELETE /api/email-accounts/{id}`
- `POST /api/email-accounts/{id}/test`
- `POST /api/email-accounts/{id}/sync`

Each account is scoped to a company and store and stores:

- name
- host
- port
- username
- encrypted password
- protocol `IMAP`
- SSL flag
- active flag

Common IMAP settings:

- Gmail: `imap.gmail.com`, port `993`, SSL enabled, app password required.
- Outlook/Microsoft 365: `outlook.office365.com`, port `993`, SSL enabled.
- Custom providers: use the host, port, username, and password supplied by the provider.

Use `POST /api/email-accounts/{id}/test` after creating the account to validate credentials and connectivity.
Use `POST /api/email-accounts/{id}/sync` to manually import unread messages.

## Scheduler

The automatic job is disabled by default:

```yaml
eai:
  email:
    importer:
      enabled: false
      fixed-delay: 60000
```

Production can enable it with:

```text
EAI_EMAIL_IMPORTER_ENABLED=true
EAI_EMAIL_IMPORTER_FIXED_DELAY=60000
```

When enabled, the scheduler imports active accounts every configured fixed delay.

## Parsing

The current parser is generic. It tries to extract:

- customer name
- phone
- e-mail
- vehicle of interest
- original message
- origin

It recognizes common labels such as `Nome`, `Telefone`, `E-mail`, `Veiculo`, `Origem`, and also falls back to regex extraction for phone and e-mail.

The application layer uses the `EmailParser` interface and a generic parser implementation. Specific parsers by provider or marketplace can be added later by implementing `EmailParser` and giving the implementation a higher priority.

## Duplicate Rule

During import, if a lead with the same normalized phone and same vehicle already exists in the same store in the previous 7 days, the imported lead is created with status `DUPLICATED`.

If no duplicate is found, the imported lead is created with source `EMAIL` and status `NEW`.

Both paths keep the original e-mail body in `originalMessage` and register lead history. Scheduler-created history entries use a system history record with no user.

## Password Security

Passwords are never returned by the API and are not stored as plain text.

The current implementation uses the `EncryptionService` interface with a development implementation based on Base64. This is only an obfuscation placeholder for local development and must be replaced in production with real encryption backed by a managed key, such as KMS, Vault, or an application secret stored outside the database.

## Limitations

- Only IMAP is supported.
- The generic parser can miss nonstandard templates.
- Messages are read from `INBOX`.
- The current reader searches unread messages and messages received after `lastReadAt`, but does not mark messages as read.
- Attachments are ignored.
- HTML-only e-mails may have limited extraction until an HTML-to-text parser is added.
