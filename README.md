# EAI

EAI e uma base SaaS para lojas de veiculos e concessionarias gerenciarem leads, equipes comerciais, conversas de WhatsApp, pipeline comercial, atendimento e relatorios.

## Stack

- Backend: Java 21, Spring Boot 4, Maven, Spring Web, Spring Security, Spring Data JPA, PostgreSQL, Flyway, Validation, Lombok, MapStruct, OpenAPI/Swagger
- Frontend: React, Vite, TypeScript, Material UI, React Router, React Query, Axios, React Hook Form, Zod
- Infra: Docker Compose e PostgreSQL

## Requisitos

- JDK Java 21. O backend esta fixado em Java 21 e o Maven falha cedo quando executado com outra versao.
- Maven 3.9+
- Node.js 20+
- npm 10+
- Docker e Docker Compose

## Subir Banco De Dados

```bash
docker compose up -d postgres
```

O PostgreSQL fica exposto em `localhost:5432`.

Credenciais padrao de desenvolvimento:

- Banco: `eai`
- Usuario: `eai`
- Senha: `eai`

## Subir Backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

O backend roda em `http://localhost:8080`.

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health endpoint:

```text
http://localhost:8080/actuator/health
```

### Seguranca Do Backend

A autenticacao de desenvolvimento usa tokens de acesso JWT e refresh tokens persistidos.

Variavel de ambiente obrigatoria em producao:

- `JWT_SECRET`: segredo HMAC usado para assinar tokens de acesso.

Configuracoes opcionais de TTL dos tokens:

- `eai.security.access-token-ttl-minutes`: padrao `15`
- `eai.security.refresh-token-ttl-hours`: padrao `168`

Usuarios seed de desenvolvimento:

- Admin: `admin@eai.com`
- Gerente: `gerente@eai.com`
- Vendedores: `ana@eai.com`, `bruno@eai.com`, `carla@eai.com`
- Recepcao: `recepcao@eai.com`
- Auditoria: `auditor@eai.com`
- Senha: `admin123`

Endpoints de autenticacao:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/auth/me`
- `POST /api/auth/logout`

Endpoints de gestao de usuarios:

- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PATCH /api/users/{id}/tenant`
- `PATCH /api/users/{id}/activate`
- `PATCH /api/users/{id}/deactivate`

Endpoints de gestao de tenants:

- `GET /api/companies`
- `GET /api/companies/{id}`
- `POST /api/companies`
- `PUT /api/companies/{id}`
- `GET /api/stores`
- `GET /api/stores?companyId={companyId}`
- `GET /api/stores/{id}`
- `POST /api/stores`
- `PUT /api/stores/{id}`

Endpoints de gestao de leads:

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

A listagem de leads e paginada e suporta filtros por status, origem, vendedor, loja, periodo, texto livre, veiculo e telefone.

Endpoints do importador de leads por e-mail:

- `GET /api/email-accounts`
- `GET /api/email-accounts/{id}`
- `POST /api/email-accounts`
- `PUT /api/email-accounts/{id}`
- `DELETE /api/email-accounts/{id}`
- `POST /api/email-accounts/{id}/test`
- `POST /api/email-accounts/{id}/sync`

O job importador IMAP fica desabilitado por padrao com `eai.email.importer.enabled=false` e usa `eai.email.importer.fixed-delay=60000`. Em producao, use `EAI_EMAIL_IMPORTER_ENABLED` e `EAI_EMAIL_IMPORTER_FIXED_DELAY`. Veja [Importador de leads por e-mail](docs/tecnico/email-importer.md) para setup IMAP, limitacoes, regras de duplicidade e seguranca de senha.

Os perfis sao `ADMIN`, `MANAGER`, `SELLER`, `RECEPTIONIST` e `AUDITOR`.
`ADMIN` pode gerenciar empresas, lojas, usuarios, vinculos de tenant e todos os leads. `MANAGER` pode visualizar usuarios e gerenciar lojas no escopo de sua empresa, e o acesso a leads e limitado a propria loja. `SELLER` fica limitado a propria loja.

O seed de desenvolvimento cria uma empresa padrao, uma loja padrao, usuarios por perfil, templates de mensagem, configuracoes de distribuicao/SLA, conta IMAP de exemplo inativa, leads em diferentes etapas do funil, historico, notas, tags, comunicacoes e follow-ups.

## Subir Frontend

```bash
cd frontend
npm install
npm run dev
```

O frontend roda em `http://localhost:5173`.

Por padrao, o frontend chama `http://localhost:8080/api`. Sobrescreva com `VITE_API_BASE_URL`.

## Build

Backend:

```bash
cd backend
mvn clean package
```

Confira a versao do Java usada pelo Maven antes do build:

```bash
java -version
mvn -version
```

Ambos os comandos devem reportar Java 21.

Frontend:

```bash
cd frontend
npm install
npm run build
```

## Estrutura Do Projeto

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

## Documentacao

- [Indice da documentacao](docs/README.md)
- [Visao](docs/negocio/vision.md)
- [Regras de negocio](docs/negocio/business-rules.md)
- [Modelo de dominio](docs/negocio/domain.md)
- [Casos de uso](docs/negocio/use-cases.md)
- [Roadmap](docs/negocio/roadmap.md)
- [Arquitetura](docs/tecnico/architecture.md)
- [Diretrizes de API](docs/tecnico/api.md)
- [Banco de dados](docs/tecnico/database.md)
- [Guia de desenvolvimento](docs/tecnico/development-guide.md)
- [Importador de leads por e-mail](docs/tecnico/email-importer.md)
- [ADRs](docs/tecnico/adr)
- [Onboarding de agentes de IA](.agents/AGENTS.md)

## Baseline De Documentacao Da Sprint 0

A Sprint 0 estabeleceu o conjunto de documentos usado como fonte oficial para desenvolvedores, Product Owner, Software Architect e agentes de IA. Trabalhos futuros de funcionalidade devem consultar os documentos relevantes antes da implementacao. Regras de negocio ausentes devem ser registradas como pendencias para o Product Owner em vez de serem assumidas.
