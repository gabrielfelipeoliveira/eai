# Architecture

## Overview

EAI is split into a Spring Boot backend, a React frontend, and PostgreSQL infrastructure managed by Docker Compose.

## Backend

The backend follows hexagonal architecture under the base package `com.eai`.

```text
com.eai
├── domain
├── application
├── infrastructure
└── api
```

### Layers

- `domain`: entities, value objects, and domain rules.
- `application`: use cases and application services.
- `infrastructure`: persistence, external integrations, framework configuration, and adapters.
- `api`: HTTP controllers, request DTOs, response DTOs, and API-specific error handling.

Controllers must not contain business rules. They should delegate to application services and translate HTTP input and output.

## Frontend

The frontend is a Vite React application using TypeScript and Material UI.

```text
frontend/src
├── app
├── components
├── features
├── hooks
├── layouts
├── pages
├── services
├── theme
└── types
```

## Database

PostgreSQL is the primary database. Flyway manages all schema migrations.

## Profiles

- `dev`: local development with Docker Compose PostgreSQL.
- `test`: test execution.
- `prod`: production deployment configuration through environment variables.
