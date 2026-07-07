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

Do not edit migrations already applied outside local experimentation. Authentication uses `V2__create_users_and_refresh_tokens.sql` for users, user roles, refresh tokens, and the BCrypt-hashed admin seed.

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

## Documentation

Update `README.md` and `docs/` whenever setup, architecture, environment variables, or development workflows change.
