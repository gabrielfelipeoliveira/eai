# AGENTS.md

Permanent instructions for AI agents working on the EAI project.

## Product Vision

EAI is a SaaS platform for automotive stores and dealerships. It helps teams manage leads, sellers, WhatsApp conversations, sales funnel stages, customer service activities, and commercial reports in one operational workspace.

## Business Objective

The commercial goal is to increase lead conversion, reduce response time, organize seller performance, and give managers reliable visibility into the dealership sales process.

## Expected Architecture

- Backend must follow hexagonal architecture.
- Backend packages must be organized under `com.eai`:
  - `domain`
  - `application`
  - `infrastructure`
  - `api`
- Domain and application layers must stay independent from framework-specific concerns whenever practical.
- Controllers belong to the API layer and must not contain business rules.
- Infrastructure contains database, external integrations, persistence adapters, and framework configuration.
- Frontend must be modular by feature and keep shared concerns in `components`, `hooks`, `services`, `theme`, and `types`.
- Keep architecture clean. Do not introduce shortcuts that couple layers unnecessarily.

## Stack

Backend:

- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Validation
- Lombok
- MapStruct
- OpenAPI/Swagger

Frontend:

- React
- Vite
- TypeScript
- Material UI
- React Router
- React Query
- Axios
- React Hook Form
- Zod

Infra:

- Docker Compose
- PostgreSQL
- Git
- Documentation in `docs/`

## Code Standards

- Always analyze the project before changing files.
- Prefer existing patterns over introducing new ones.
- Do not duplicate code. Extract shared behavior only when it removes real duplication or clarifies intent.
- Keep changes small, focused, and consistent with the architecture.
- Use clear names for classes, methods, variables, components, and files.
- Avoid unrelated refactors in feature or fix commits.
- Keep comments rare and useful.

## Commit Standard

Use conventional commits:

- `feat: ...` for new user-facing functionality
- `fix: ...` for bug fixes
- `chore: ...` for tooling, setup, or maintenance
- `docs: ...` for documentation-only changes
- `test: ...` for test changes
- `refactor: ...` for behavior-preserving code changes

Commit messages must be concise and describe the actual change.

## Definition of Done

A task is done only when:

- Code compiles.
- Relevant tests pass or missing tests are explicitly documented.
- New behavior is documented when needed.
- Architecture boundaries are respected.
- No business rule is placed in controllers.
- No unnecessary duplication is introduced.
- Configuration changes are documented.
- The final response lists validations performed.

## Backend Rules

- Keep business logic out of controllers.
- Use controllers only for HTTP request and response handling.
- Put application use cases in the application layer.
- Put entities, value objects, and domain rules in the domain layer.
- Put Spring Data repositories, JPA entities, external clients, and config in infrastructure.
- Use DTOs for API input and output.
- Use Bean Validation for request validation.
- Use MapStruct for object mapping when mapping logic becomes non-trivial.
- Keep profile-specific settings in `application-dev.yml`, `application-test.yml`, and `application-prod.yml`.
- Do not leak persistence entities directly through API responses.

## Frontend Rules

- Use TypeScript strictly.
- Use Material UI for UI primitives and theming.
- Use React Router for navigation.
- Use React Query for server state.
- Use Axios through service modules, not directly inside page components when avoidable.
- Use React Hook Form and Zod for forms and validation.
- Keep layout components in `layouts`.
- Keep route-level screens in `pages`.
- Keep reusable UI components in `components`.
- Keep feature-specific code in `features`.
- Keep API clients in `services`.

## Database Rules

- PostgreSQL is the primary database.
- Flyway owns schema migrations.
- Do not modify existing migrations after they are applied outside local experimentation.
- Create new migrations for schema changes.
- Keep migrations deterministic and reviewable.
- Prefer explicit database constraints for important invariants.

## Test Rules

- Backend tests should use JUnit and Spring Boot test support.
- Prefer focused unit tests for domain and application behavior.
- Use integration tests for database and API boundaries when behavior depends on Spring or persistence.
- Frontend tests should cover critical user flows and reusable logic when introduced.
- Do not skip failing tests without documenting the reason.

## Documentation Rules

- Keep `README.md` updated with setup, run, and build instructions.
- Keep `docs/` updated when architecture, workflows, or product direction changes.
- Document new environment variables.
- Document Docker Compose changes.
- Prefer concise, actionable documentation.
