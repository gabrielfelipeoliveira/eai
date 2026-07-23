# Guia De Desenvolvimento

## Configuracao Local

O backend exige JDK Java 21. Nao compile nem execute com Java 17 ou qualquer outra versao. Antes de iniciar, confirme que `java -version`, `mvn -version` e `JAVA_HOME` apontam para Java 21.

Em maquinas Windows de desenvolvimento, configure os padroes do usuario para Java 21:

```powershell
$jdk21 = 'C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot'
[Environment]::SetEnvironmentVariable('JAVA_HOME', $jdk21, 'User')
[Environment]::SetEnvironmentVariable('Path', "$jdk21\bin;" + [Environment]::GetEnvironmentVariable('Path', 'User'), 'User')
```

Abra um novo terminal depois de alterar variaveis de ambiente do usuario.

Subir o PostgreSQL:

```bash
docker compose up -d postgres
```

Executar o backend:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Executar o backend com dados demonstrativos locais:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev,demo
```

Executar o frontend:

```bash
cd frontend
npm ci
npm run dev
```

Executar testes unitarios e de componentes do frontend:

```bash
cd frontend
npm test
```

Use `npm run test:watch` durante desenvolvimento local quando quiser rerodar os testes a cada alteracao. Novos testes frontend devem descrever o comportamento em PT-BR e priorizar fluxos observaveis pelo usuario, validacoes de formulario, autorizacao por papel e hooks compartilhados.

Executar testes E2E do frontend:

```bash
cd frontend
npm run setup:e2e
npm run test:e2e
```

A suite E2E sobe o Vite local automaticamente e usa API mockada nos testes para validar os fluxos criticos no navegador sem exigir backend ou banco locais.

Os scripts do frontend devem funcionar em Windows, macOS e Linux. Evite comandos especificos de shell Unix ou PowerShell em `package.json`; quando precisar tratar variaveis de ambiente, arquivos ou encadear comportamento, prefira scripts Node em `frontend/scripts/*.mjs`.

Credenciais seed de desenvolvimento:

- Admin: `admin@eai.com`
- Gerente: `gerente@eai.com`
- Gerente de loja: `gerente.loja@eai.com`
- Pre-venda: `pre.venda@eai.com`
- F&I: `fi@eai.com`
- Vendedor: `ana@eai.com`
- Avaliador: `avaliador@eai.com`
- Senha: `admin123`

O frontend espera a API do backend em `http://localhost:8080/api`. Defina `VITE_API_BASE_URL` ao usar uma URL diferente para o backend.

## Build Do Backend

```bash
cd backend
mvn clean package
```

O backend exige Java 21. O Maven Enforcer falha o build quando o Maven nao esta rodando com Java 21. Se o Maven reportar erro de versao Java, ajuste `JAVA_HOME` e o Java no `PATH`.

## Build Do Frontend

```bash
cd frontend
npm run build
```

## Migrations Do Banco

As migrations do Flyway ficam em:

```text
backend/src/main/resources/db/migration
```

Seeds locais ficam separados das migrations de schema:

```text
backend/src/main/resources/db/seed/mandatory
backend/src/main/resources/db/seed/demo
```

Crie novas migrations com o padrao de nome:

```text
V{numero}__descricao.sql
```

Nao edite migrations ja aplicadas fora de experimentacao local. Como a base ainda e descartavel, o schema inicial esta consolidado em `V1__initial_schema.sql`; seeds obrigatorios rodam em `dev` e `test`, enquanto dados de demonstracao rodam somente quando o perfil `demo` esta ativo. Producao usa apenas `classpath:db/migration`.

## Smoke Test De Autenticacao

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@eai.com\",\"password\":\"admin123\"}"
```

Endpoint protegido sem token:

```bash
curl -i http://localhost:8080/api/users
```

O resultado esperado e `401 Unauthorized`.

## Smoke Test De Metadados

O catalogo de metadados e publico e deve retornar labels de apresentacao para codigos tecnicos:

```bash
curl -H "Accept-Language: pt-BR" http://localhost:8080/api/metadata
```

O resultado esperado inclui `locale`, `leadStatuses`, `leadSources`, `userRoles`, `tenantStatuses`, `messageTemplateTypes`, `leadDistributionModes`, `emailAccountStatuses` e `emailProtocols`.

Clientes devem usar este catalogo para exibir labels em vez de mostrar enums crus ao usuario.

## Smoke Test De Tenant

Use o login do admin seed para obter um token de acesso e chame:

```bash
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/companies
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/stores
```

A empresa padrao e a loja padrao devem ser retornadas.

## Smoke Test De Lead

Use o login do admin seed para obter um token de acesso. Os ids padrao de tenant sao:

- Empresa: `00000000-0000-0000-0000-000000000101`
- Loja: `00000000-0000-0000-0000-000000000201`

Criar um lead manual:

```bash
curl -X POST http://localhost:8080/api/leads \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"companyId\":\"00000000-0000-0000-0000-000000000101\",\"storeId\":\"00000000-0000-0000-0000-000000000201\",\"customerName\":\"Cliente Teste\",\"customerPhone\":\"11999990000\",\"vehicleInterest\":\"Honda Civic\",\"source\":\"MANUAL\"}"
```

O status esperado e `AVAILABLE`.

Listar, atribuir, alterar status, adicionar observacao e consultar historico:

```bash
curl -H "Authorization: Bearer <access-token>" "http://localhost:8080/api/leads?page=0&size=20"
curl -X PATCH -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/<lead-id>/assign-to-me
curl -X PATCH http://localhost:8080/api/leads/<lead-id>/status -H "Authorization: Bearer <access-token>" -H "Content-Type: application/json" -d "{\"status\":\"FIRST_CONTACT\",\"description\":\"Primeiro contato realizado\"}"
curl -X POST http://localhost:8080/api/leads/<lead-id>/notes -H "Authorization: Bearer <access-token>" -H "Content-Type: application/json" -d "{\"note\":\"Cliente pediu proposta por WhatsApp\"}"
curl -X PUT http://localhost:8080/api/leads/<lead-id>/notes/<note-id> -H "Authorization: Bearer <access-token>" -H "Content-Type: application/json" -d "{\"note\":\"Cliente pediu proposta atualizada por WhatsApp\"}"
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/<lead-id>/history
```

Tags de lead usam catalogo global:

```bash
curl -X POST http://localhost:8080/api/leads/tags/catalog -H "Authorization: Bearer <access-token>" -H "Content-Type: application/json" -d "{\"name\":\"VIP\",\"type\":\"PRIORITY\"}"
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/tags/catalog
curl -X POST http://localhost:8080/api/leads/<lead-id>/tags -H "Authorization: Bearer <access-token>" -H "Content-Type: application/json" -d "{\"tagId\":\"<tag-id>\"}"
```

Consultar o pipeline agrupado por status:

```bash
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/pipeline
```

Criar e concluir um follow-up:

```bash
curl -X POST http://localhost:8080/api/leads/<lead-id>/follow-ups \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Retornar proposta\",\"description\":\"Enviar simulacao atualizada\",\"dueAt\":\"2026-07-08T13:00:00Z\"}"
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/follow-ups/my
curl -X PATCH -H "Authorization: Bearer <access-token>" http://localhost:8080/api/follow-ups/<follow-up-id>/complete
```

Configurar distribuicao e SLA para a loja padrao:

```bash
curl -X PUT http://localhost:8080/api/distribution/config \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"companyId\":\"00000000-0000-0000-0000-000000000101\",\"storeId\":\"00000000-0000-0000-0000-000000000201\",\"mode\":\"ROUND_ROBIN\",\"active\":true,\"minutesToAssign\":15,\"minutesToFirstContact\":30,\"slaActive\":true}"
```

Comandos de distribuicao automatica:

```bash
curl -X POST -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/<lead-id>/assign-automatically
curl -X POST -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/distribute-pending
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/sla/overdue
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/dashboard/leads
```

`ROUND_ROBIN` atribui ao proximo vendedor ativo depois da ultima atribuicao na loja. `LEAST_BUSY` atribui ao vendedor ativo com menos leads abertos.

Gerar um link do WhatsApp usando o template seed de primeiro contato e consultar o historico de comunicacao:

```bash
curl -X POST http://localhost:8080/api/leads/<lead-id>/whatsapp-link \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"templateId\":\"00000000-0000-0000-0000-000000000301\"}"
curl -H "Authorization: Bearer <access-token>" http://localhost:8080/api/leads/<lead-id>/communications
```

## Smoke Test Do Importador De E-Mail

O scheduler IMAP fica desabilitado por padrao em desenvolvimento:

```yaml
eai.email.importer.enabled=false
eai.email.importer.fixed-delay=60000
```

Criar uma conta de e-mail:

```bash
curl -X POST http://localhost:8080/api/email-accounts \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d "{\"companyId\":\"00000000-0000-0000-0000-000000000101\",\"storeId\":\"00000000-0000-0000-0000-000000000201\",\"name\":\"Leads IMAP\",\"host\":\"imap.example.com\",\"port\":993,\"username\":\"leads@example.com\",\"password\":\"secret\",\"protocol\":\"IMAP\",\"useSsl\":true,\"active\":true}"
```

Testar e sincronizar manualmente:

```bash
curl -X POST -H "Authorization: Bearer <access-token>" http://localhost:8080/api/email-accounts/<account-id>/test
curl -X POST -H "Authorization: Bearer <access-token>" http://localhost:8080/api/email-accounts/<account-id>/sync
```

Veja [Importador de leads por e-mail](email-importer.md) para configuracao IMAP, limitacoes, regras de duplicidade e observacoes de seguranca de senha.

## Documentacao

Pontos de entrada da documentacao oficial:

- [Visao](../negocio/vision.md)
- [Regras de negocio](../negocio/business-rules.md)
- [Modelo de dominio](../negocio/domain.md)
- [Casos de uso](../negocio/use-cases.md)
- [Arquitetura](architecture.md)
- [Diretrizes de API](api.md)
- [Banco de dados](database.md)
- [Roadmap](../negocio/roadmap.md)
- [Importador de leads por e-mail](email-importer.md)
- [ADRs](adr/)
