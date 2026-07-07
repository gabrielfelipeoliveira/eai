# Development Guide

## Local Setup

The backend requires Java 21 JDK. Do not build or run it with Java 17 or any other Java version. Confirm `java -version`, `mvn -version`, and `JAVA_HOME` point to Java 21 before starting.

On Windows development machines, set the user defaults to Java 21:

```powershell
$jdk21 = 'C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot'
[Environment]::SetEnvironmentVariable('JAVA_HOME', $jdk21, 'User')
[Environment]::SetEnvironmentVariable('Path', "$jdk21\bin;" + [Environment]::GetEnvironmentVariable('Path', 'User'), 'User')
```

Open a new terminal after changing user environment variables.

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the backend:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Run the frontend:

```bash
cd frontend
npm install
npm run dev
```

Seed admin credentials:

- Email: `admin@eai.com`
- Password: `admin123`

The frontend expects the backend API at `http://localhost:8080/api`. Set `VITE_API_BASE_URL` when using a different backend URL.

## Backend Build

```bash
cd backend
mvn clean package
```

The backend requires Java 21. Maven Enforcer fails the build when Maven is not running on Java 21. If Maven reports a Java version error, fix `JAVA_HOME` and the Java on `PATH`.

## Frontend Build

```bash
cd frontend
npm run build
```

## Database Migrations

Flyway migrations live in:

```text
backend/src/main/resources/db/migration
```

Create new migrations with the naming pattern:

```text
V{number}__description.sql
```

Do not edit migrations already applied outside local experimentation. Authentication uses `V2__create_users_and_refresh_tokens.sql` for users, user roles, refresh tokens, and the BCrypt-hashed admin seed. Tenant setup uses `V3__add_companies_stores_and_user_tenant_links.sql` for companies, stores, user tenant columns, the default company seed, the default store seed, and the admin tenant link.

## Authentication Smoke Test

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@eai.com\",\"password\":\"admin123\"}"
```

Protected endpoint without token:

```bash
curl -i http://localhost:8080/api/users
```

Expected result is `401 Unauthorized`.

## Tenant Smoke Test

Use the seed admin login to obtain an access token, then call:

```bash
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/companies
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/stores
```

The default company and default store should be returned.

## Lead Smoke Test

Use the seed admin login to obtain an access token. The default tenant ids are:

- Company: `00000000-0000-0000-0000-000000000101`
- Store: `00000000-0000-0000-0000-000000000201`

Create a manual lead:

```bash
curl -X POST http://localhost:8080/api/leads \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"companyId\":\"00000000-0000-0000-0000-000000000101\",\"storeId\":\"00000000-0000-0000-0000-000000000201\",\"customerName\":\"Cliente Teste\",\"customerPhone\":\"11999990000\",\"vehicleInterest\":\"Honda Civic\",\"source\":\"MANUAL\"}"
```

Expected status is `AVAILABLE`.

List, assign, change status, add a note, and inspect history:

```bash
curl -H "Authorization: Bearer <access-token>" "http://localhost:8080/api/leads?page=0&size=20"
curl -X PATCH -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/<lead-id>/assign-to-me
curl -X PATCH http://localhost:8080/api/leads/<lead-id>/status -H "Authorization: Bearer <access-token>" -H "Content-Type: application/json" -d "{\"status\":\"FIRST_CONTACT\",\"description\":\"Primeiro contato realizado\"}"
curl -X POST http://localhost:8080/api/leads/<lead-id>/notes -H "Authorization: Bearer <access-token>" -H "Content-Type: application/json" -d "{\"note\":\"Cliente pediu proposta por WhatsApp\"}"
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/<lead-id>/history
```

Inspect the pipeline grouped by status:

```bash
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/pipeline
```

Create and complete a follow-up:

```bash
curl -X POST http://localhost:8080/api/leads/<lead-id>/follow-ups \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Retornar proposta\",\"description\":\"Enviar simulacao atualizada\",\"dueAt\":\"2026-07-08T13:00:00Z\"}"
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/follow-ups/my
curl -X PATCH -H "Authorization: Bearer <access-token>" http://localhost:8080/api/follow-ups/<follow-up-id>/complete
```

Configure distribution and SLA for the default store:

```bash
curl -X PUT http://localhost:8080/api/distribution/config \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"companyId\":\"00000000-0000-0000-0000-000000000101\",\"storeId\":\"00000000-0000-0000-0000-000000000201\",\"mode\":\"ROUND_ROBIN\",\"active\":true,\"minutesToAssign\":15,\"minutesToFirstContact\":30,\"slaActive\":true}"
```

Automatic distribution commands:

```bash
curl -X POST -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/<lead-id>/assign-automatically
curl -X POST -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/distribute-pending
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/sla/overdue
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/dashboard/leads
```

`ROUND_ROBIN` assigns the next active seller after the latest assignment in the store. `LEAST_BUSY` assigns to the active seller with the fewest open leads.

Generate a WhatsApp link using the seeded first-contact template and inspect the communication history:

```bash
curl -X POST http://localhost:8080/api/leads/<lead-id>/whatsapp-link \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"templateId\":\"00000000-0000-0000-0000-000000000301\"}"
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/<lead-id>/communications
```

## Email Importer Smoke Test

The IMAP scheduler is disabled by default in development:

```yaml
eai.email.importer.enabled=false
eai.email.importer.fixed-delay=60000
```

Create an e-mail account:

```bash
curl -X POST http://localhost:8080/api/email-accounts \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"companyId\":\"00000000-0000-0000-0000-000000000101\",\"storeId\":\"00000000-0000-0000-0000-000000000201\",\"name\":\"Leads IMAP\",\"host\":\"imap.example.com\",\"port\":993,\"username\":\"leads@example.com\",\"password\":\"secret\",\"protocol\":\"IMAP\",\"useSsl\":true,\"active\":true}"
```

Test and manually sync:

```bash
curl -X POST -H "Authorization: Bearer <access-token>" http://localhost:8080/api/email-accounts/<account-id>/test
curl -X POST -H "Authorization: Bearer <access-token>" http://localhost:8080/api/email-accounts/<account-id>/sync
```

See [Email Lead Importer](email-importer.md) for IMAP setup, limitations, duplicate rules, and password security notes.

## Documentation

Update `README.md` and `docs/` whenever setup, architecture, environment variables, or development workflows change.
