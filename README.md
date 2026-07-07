# EAI

EAI is a SaaS foundation for vehicle stores and dealerships to manage leads, sales teams, WhatsApp conversations, commercial pipeline, customer service, and reports.

## Stack

- Backend: Java 21, Spring Boot 3, Maven, Spring Web, Spring Security, Spring Data JPA, PostgreSQL, Flyway, Validation, Lombok, MapStruct, OpenAPI/Swagger
- Frontend: React, Vite, TypeScript, Material UI, React Router, React Query, Axios, React Hook Form, Zod
- Infra: Docker Compose and PostgreSQL

## Requirements

- Java 21
- Maven 3.9+
- Node.js 20+
- npm 10+
- Docker and Docker Compose

## Run Database

```bash
docker compose up -d postgres
```

PostgreSQL is exposed on `localhost:5432`.

Default development credentials:

- Database: `eai`
- User: `eai`
- Password: `eai`

## Run Backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend runs on `http://localhost:8080`.

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

### Backend Security

Development authentication uses JWT access tokens and persisted refresh tokens.

Required production environment variable:

- `JWT_SECRET`: HMAC secret used to sign access tokens.

Optional token TTL settings:

- `eai.security.access-token-ttl-minutes`: default `15`
- `eai.security.refresh-token-ttl-hours`: default `168`

Seed admin user:

- Email: `admin@eai.com`
- Password: `admin123`

Authentication endpoints:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/auth/me`
- `POST /api/auth/logout`

User management endpoints:

- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PATCH /api/users/{id}/tenant`
- `PATCH /api/users/{id}/activate`
- `PATCH /api/users/{id}/deactivate`

Tenant management endpoints:

- `GET /api/companies`
- `GET /api/companies/{id}`
- `POST /api/companies`
- `PUT /api/companies/{id}`
- `GET /api/stores`
- `GET /api/stores?companyId={companyId}`
- `GET /api/stores/{id}`
- `POST /api/stores`
- `PUT /api/stores/{id}`

Roles are `ADMIN`, `MANAGER`, `SELLER`, `RECEPTIONIST`, and `AUDITOR`.
`ADMIN` can manage companies, stores, users, and user tenant links. `MANAGER` can view users and manage stores in its company scope. `SELLER` is scoped to its own store.

The development seed creates a default company and store and links the seed admin user to both.

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

The frontend calls `http://localhost:8080/api` by default. Override it with `VITE_API_BASE_URL`.

## Build

Backend:

```bash
cd backend
mvn clean package
```

Frontend:

```bash
cd frontend
npm install
npm run build
```

## Project Structure

```text
/
├── backend
├── frontend
├── docs
├── scripts
├── docker
├── README.md
├── AGENTS.md
├── docker-compose.yml
└── .gitignore
```

## Documentation

- [Product vision](docs/product-vision.md)
- [Architecture](docs/architecture.md)
- [Roadmap](docs/roadmap.md)
- [Development guide](docs/development-guide.md)
