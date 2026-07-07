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

## Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`.

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
