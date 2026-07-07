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

## Backend Build

```bash
cd backend
mvn clean package
```

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

## Documentation

Update `README.md` and `docs/` whenever setup, architecture, environment variables, or development workflows change.
