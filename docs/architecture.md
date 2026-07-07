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

### Authentication and Users

Authentication is implemented with Spring Security and stateless JWT access tokens. Refresh tokens are persisted in PostgreSQL so logout can revoke active sessions logically.

User domain rules live in `com.eai.domain.user`. Application use cases and ports live in `com.eai.application`. Persistence adapters, JWT signing, BCrypt hashing, and Spring Security configuration live in `com.eai.infrastructure`. HTTP DTOs and controllers live in `com.eai.api`.

Tenant domain rules live in `com.eai.domain.tenant`. A `Company` represents a SaaS customer organization, and a `Store` belongs to one company. Users must be linked to a `companyId` and `storeId`; sellers are store-scoped.

Mandatory roles are `ADMIN`, `MANAGER`, `SELLER`, `RECEPTIONIST`, and `AUDITOR`.

Permission rules:

- `ADMIN`: can view and manage companies, stores, users, and user tenant links.
- `MANAGER`: can view users and manage stores in its company scope. If linked to a specific store, visibility is limited to that store.
- `SELLER`: can only access store-scoped data for its own store.

Authentication endpoints:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/auth/me`
- `POST /api/auth/logout`

User endpoints:

- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PATCH /api/users/{id}/tenant`
- `PATCH /api/users/{id}/activate`
- `PATCH /api/users/{id}/deactivate`

Company endpoints:

- `GET /api/companies`
- `GET /api/companies/{id}`
- `POST /api/companies`
- `PUT /api/companies/{id}`

Store endpoints:

- `GET /api/stores`
- `GET /api/stores?companyId={companyId}`
- `GET /api/stores/{id}`
- `POST /api/stores`
- `PUT /api/stores/{id}`

Lead management is implemented as the first Core CRM module. Lead domain objects live in `com.eai.domain.lead`; use cases and repository ports live in `com.eai.application.lead`; JPA entities, Spring Data repositories, and persistence adapters live in `com.eai.infrastructure.persistence.lead`; HTTP DTOs and controllers live in `com.eai.api.lead`.

Lead visibility is store-scoped for `SELLER` and `MANAGER`. `ADMIN` can access all leads. Manual leads start as `AVAILABLE`; automatic sources start as `NEW`. Status changes and assignments create lead history records.

Lead endpoints:

- `POST /api/leads`
- `GET /api/leads`
- `GET /api/leads/{id}`
- `PUT /api/leads/{id}`
- `PATCH /api/leads/{id}/status`
- `PATCH /api/leads/{id}/assign-to-me`
- `PATCH /api/leads/{id}/assign/{userId}`
- `POST /api/leads/{id}/notes`
- `GET /api/leads/{id}/history`
- `GET /api/leads/{id}/notes`
- `POST /api/leads/{id}/tags`
- `GET /api/leads/{id}/tags`
- `DELETE /api/leads/{id}/tags/{tagId}`
- `POST /api/leads/{id}/whatsapp-link`
- `GET /api/leads/{id}/communications`

Communication templates are implemented in `com.eai.domain.message`, `com.eai.application.message`, `com.eai.infrastructure.persistence.message`, and `com.eai.api.message`. Templates are store-scoped and support placeholders for customer, phone, vehicle, seller, store, and city. WhatsApp link generation renders the selected active template, creates a `wa.me` URL, and records a lead communication entry.

Template endpoints:

- `GET /api/templates`
- `GET /api/templates/{id}`
- `GET /api/templates/active`
- `POST /api/templates`
- `PUT /api/templates/{id}`
- `DELETE /api/templates/{id}`

`GET /api/leads` is always paginated and supports filters for status, source, assigned seller, store, creation period, free text, vehicle, and phone.

Email lead import is implemented in `com.eai.domain.email`, `com.eai.application.email`, `com.eai.infrastructure.persistence.email`, `com.eai.infrastructure.email`, and `com.eai.api.email`. E-mail accounts are store-scoped, use IMAP, and store encrypted passwords through the `EncryptionService` port. Import logic stays in application services: `EmailReader`, `EmailParser`, `LeadExtractor`, `DuplicateLeadChecker`, and `EmailLeadImporter`. Imported leads use source `EMAIL`; possible duplicates are marked with status `DUPLICATED`.

Email account endpoints:

- `GET /api/email-accounts`
- `GET /api/email-accounts/{id}`
- `POST /api/email-accounts`
- `PUT /api/email-accounts/{id}`
- `DELETE /api/email-accounts/{id}`
- `POST /api/email-accounts/{id}/test`
- `POST /api/email-accounts/{id}/sync`

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

The frontend stores the access token and refresh token in browser storage through `services/tokenStorage`. Axios is configured in `services/api` to attach bearer tokens and refresh expired access tokens. Route protection is centralized in `components/ProtectedRoute`, while authenticated user state is exposed through `hooks/useAuth`.

The authenticated layout uses a lateral menu with Dashboard, Leads, Usuarios, Empresas, Lojas, Templates, E-mails, and Configuracoes. Empresas is visible only to `ADMIN`. Lojas, Usuarios, Templates, and E-mails are visible to `ADMIN` and `MANAGER`; user creation and tenant linking are available only to `ADMIN`.

The Leads screen is available at `/leads`. It provides CRM-style status cards, filters, a paginated table, lead creation, lead detail drawer, status chips, source chips, quick assignment, notes, tags, and history timeline.

## Database

PostgreSQL is the primary database. Flyway manages all schema migrations.

## Profiles

- `dev`: local development with Docker Compose PostgreSQL.
- `test`: test execution.
- `prod`: production deployment configuration through environment variables.

Production requires `JWT_SECRET` for access token signing.
