# Development Guide

## Local Setup

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

The backend requires Java 21. If Maven reports `release version 21 not supported`, check `java -version`, `mvn -version`, and `JAVA_HOME`.

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

## Documentation

Update `README.md` and `docs/` whenever setup, architecture, environment variables, or development workflows change.
