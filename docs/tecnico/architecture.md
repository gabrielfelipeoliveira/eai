# Arquitetura

Este documento e a referencia oficial de arquitetura do EAI. Decisoes de negocio ficam em `docs/negocio/business-rules.md`; casos de uso ficam em `docs/negocio/use-cases.md`; convencoes de API ficam em `docs/tecnico/api.md`; convencoes de banco ficam em `docs/tecnico/database.md`.

## Visao Geral

O EAI e um monorepo com:

- Backend Spring Boot.
- Frontend React/Vite.
- Infraestrutura PostgreSQL gerenciada localmente via Docker Compose.
- Documentacao em `docs/`.
- Onboarding de agentes em `.agents/`.

## Arquitetura do Backend

O backend segue arquitetura hexagonal, tambem conhecida como Ports and Adapters.

Pacote base:

```text
com.eai
├── domain
├── application
├── infrastructure
└── api
```

## Camadas do Backend

### Domain

Responsabilidades:

- Entidades de dominio.
- Value objects quando forem introduzidos.
- Invariantes de dominio.
- Comportamento de dominio que nao exige infraestrutura.
- Vocabulario do dominio.

Regras:

- Nao deve depender de Spring.
- Nao deve depender de JPA.
- Nao deve depender de DTOs HTTP.
- Nao deve conhecer banco de dados, controllers ou servicos externos.

### Application

Responsabilidades:

- Casos de uso.
- Servicos de aplicacao.
- Portas exigidas pelos casos de uso.
- Orquestracao transacional enquanto a implementacao atual baseada em Spring for mantida.
- Politicas de autorizacao e escopo de tenant ate que sejam extraidas para politicas dedicadas de aplicacao.

Regras:

- Nao deve expor entidades de persistencia.
- Nao deve depender de DTOs da API.
- Deve depender de portas em vez de adapters concretos de infraestrutura.
- Deve manter comportamento de negocio fora dos controllers.

Divida tecnica atual:

- Servicos de aplicacao atualmente usam anotacoes Spring como `@Service`, `@Transactional` e, em um caso, utilitarios Spring. Isso e aceito como estado atual, mas trabalho arquitetural futuro deve decidir se mantem essa integracao pragmatica com Spring ou move preocupacoes de framework para fora dos servicos de aplicacao.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Anotacoes Spring devem permanecer na camada de aplicacao?
- Boundaries transacionais devem ir para infraestrutura/configuracao?
- Autorizacao e escopo de tenant devem ser centralizados em politicas reutilizaveis de aplicacao?

### Infrastructure

Responsabilidades:

- Entidades JPA.
- Repositorios Spring Data.
- Adapters de persistencia.
- Integracoes externas.
- Detalhes de implementacao de seguranca.
- Configuracao de framework.
- Implementacao de schedulers.
- Configuracao de OpenAPI e Spring Security.

Regras:

- Implementa portas da aplicacao.
- Contem detalhes especificos de framework.
- Converte modelos de persistencia para modelos de dominio e vice-versa.
- Nao deve conter orquestracao de casos de uso de negocio.

### API

Responsabilidades:

- Controllers HTTP.
- DTOs de requisicao.
- DTOs de resposta.
- Tratamento de erros da API.
- Validacao especifica de HTTP.
- Metadados de apresentacao para codigos tecnicos visiveis em clientes.

Regras:

- Controllers apenas orquestram o tratamento da requisicao.
- Controllers delegam comportamento de negocio para servicos de aplicacao.
- Controllers nao devem conter regras de negocio.
- Controllers nao devem expor entidades de persistencia.
- Codigos tecnicos de enums podem sair pela API, mas labels para usuario devem vir de catalogos de apresentacao.

Divida tecnica atual:

- Alguns enriquecimentos de resposta acontecem atualmente em controllers, como indicadores de SLA em respostas de lead. Isso deve ser revisado e provavelmente movido para um assembler de resposta ou caso de uso de consulta.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Assemblers de resposta devem ficar na camada de API ou aplicacao?
- Modelos de resposta especificos para consultas devem ser introduzidos?

## Padrao De Lombok No Backend

Lombok faz parte da stack do backend e pode ser usado para reduzir boilerplate, desde que a mudanca seja retrocompativel e nao altere logica de dominio, contrato publico, serializacao ou comportamento runtime.

Regras:

- Novas classes backend devem nascer seguindo este padrao de Lombok seguro quando aplicavel, em vez de introduzir boilerplate para ser corrigido depois.
- Classes de dominio usam no maximo `@Getter` por padrao. Nao use `@Setter` amplo nem `@Data` em dominio.
- Entidades JPA podem usar `@Getter` e `@Setter`, mas nao devem usar `@Data`.
- Services, controllers, adapters e configuracoes podem usar `@RequiredArgsConstructor` quando todos os campos injetados forem `final` e o construtor gerado for equivalente ao atual.
- Preserve o nivel de acesso do construtor existente. Se o construtor atual for package-private, use `@RequiredArgsConstructor(access = AccessLevel.PACKAGE)`.
- DTOs, commands e responses que ja usam `record` devem continuar como `record`.
- `@Data` nao e padrao do projeto. So considere em classe simples, sem JPA, sem dominio, sem dado sensivel, sem colecoes mutaveis relevantes e sem risco de `equals`, `hashCode` ou `toString`.
- Getters defensivos, normalizacao, validacao, transicoes de estado e atualizacao de timestamps devem continuar explicitos.

## Relatorios Gerenciais

Relatorios gerenciais sao implementados em `com.eai.application.report`, com DTOs HTTP e controllers em `com.eai.api.report` e adapters de exportacao em infraestrutura.

Os relatorios reutilizam a porta de filtragem do repositorio de leads em vez de introduzir consultas separadas para cada visao. CSV e o formato inicial de exportacao por meio da porta `ReportExporter`, permitindo adicionar exporters XLSX e PDF depois sem alterar os casos de uso de relatorio.

Endpoints de relatorio:

- `GET /api/reports/leads`
- `GET /api/reports/sellers`
- `GET /api/reports/sources`
- `GET /api/reports/lost`
- `GET /api/reports/sales`
- `GET /api/reports/sla`
- `GET /api/reports/leads/export.csv`
- `GET /api/reports/sellers/export.csv`

Os filtros de relatorio suportam periodo de criacao, loja, vendedor, origem e empresa para admins. Relatorios gerenciais completos ficam para segunda fase. Quando habilitados, a visibilidade deve seguir o escopo de `ADMIN`, `MANAGER`, `STORE_MANAGER` e demais papeis autorizados; vendedores ficam limitados aos proprios leads.

## Metadados E Internacionalizacao

O backend expoe `GET /api/metadata` para catalogos de apresentacao de enums e outros codigos tecnicos.

Decisao atual:

- Banco, dominio e APIs de negocio continuam usando codigos tecnicos estaveis, como `FIRST_CONTACT`, `ROUND_ROBIN` e `SUCCESS`.
- Clientes apresentam labels localizadas vindas do catalogo de metadados.
- A primeira localizacao suportada e `pt-BR`.
- O catalogo retorna tambem `labelKey`, preparando evolucao para multi-idioma sem trocar contratos de negocio.
- O endpoint fica na camada `api`, pois trata de apresentacao e nao altera regras de dominio.
- O dominio continua independente de Spring e nao conhece labels de interface.

No frontend web:

- `useMetadata` carrega o catalogo via React Query.
- A chave de cache e composta por locale.
- O tempo de freshness atual e 24 horas.
- Componentes devem usar `metadata.label(...)`, `metadata.color(...)` e `metadata.options(...)` em vez de renderizar enums diretamente.

Para app mobile futuro:

- Carregar metadados na inicializacao da sessao ou no primeiro uso.
- Cachear por locale.
- Revalidar por janela de tempo, versao ou ETag quando essa politica for definida.

## Importacao De Leads Por E-Mail

A importacao de leads por e-mail e implementada em `com.eai.domain.email`, `com.eai.application.email`, `com.eai.infrastructure.persistence.email`, `com.eai.infrastructure.email` e `com.eai.api.email`.

Contas de e-mail pertencem ao escopo de uma loja, usam IMAP e armazenam senhas criptografadas por meio da porta `EncryptionService`. A logica de importacao permanece nos servicos de aplicacao: `EmailReader`, `EmailParser`, `LeadExtractor`, `DuplicateLeadChecker` e `EmailLeadImporter`.

Leads importados usam origem `EMAIL`; possiveis duplicidades sao marcadas com status `DUPLICATED`.

Falhas de teste de conexao e importacao de contas de e-mail criam notificacoes internas para usuarios ativos com papel `ADMIN`. O envio externo futuro deve implementar a porta generica `NotificationDeliveryPort`; a implementacao atual `NoOpNotificationDeliveryPort` nao envia e-mail externo e mantem `externalDeliveryStatus` como `PENDING_EXTERNAL_DELIVERY`.

## Notificacoes Internas

Notificacoes internas sao implementadas em `com.eai.domain.notification`, `com.eai.application.notification`, `com.eai.infrastructure.persistence.notification` e `com.eai.api.notification`.

O modulo persiste notificacoes por destinatario, tipo, severidade, titulo, mensagem, entidade relacionada, leitura e status de entrega externa. O tipo inicial e `EMAIL_ACCOUNT_FAILURE`; as severidades iniciais sao `INFO`, `WARNING` e `ERROR`.

## Conversas De WhatsApp

A persistencia de conversas de WhatsApp e implementada em `com.eai.domain.conversation`, `com.eai.application.conversation`, `com.eai.infrastructure.persistence.conversation` e `com.eai.api.conversation`.

A auditoria tecnica de acesso de gestores e admins a conversas fica no mesmo contexto de conversas, com entidade de dominio `ConversationAccessAudit`, porta de aplicacao `ConversationAccessAuditRepository` e adapter JPA em infraestrutura. Tela de auditoria e escopo operacional de `AUDITOR` ficam fora do MVP e para fase posterior.

O webhook publico continua em `com.eai.api.whatsapp` e delega para `WhatsAppWebhookService`, que extrai mensagens do payload da Meta e chama `ConversationService`. A resolucao de tenant do webhook usa as propriedades `eai.whatsapp.cloud-api.company-id` e `eai.whatsapp.cloud-api.store-id` enquanto a regra oficial de mapeamento por numero/conta nao estiver definida.

Mensagens recebidas sao armazenadas como `INBOUND` com status `RECEIVED`. O fluxo existente de geracao de link de WhatsApp registra uma mensagem `OUTBOUND` do tipo `TEMPLATE` com status `SENT`, alem do registro legado em `lead_communications`.

O envio ativo de templates aprovados pela WhatsApp Cloud API e implementado por `WhatsAppTemplateSenderService` na aplicacao e por `WhatsAppCloudTemplateClient` na infraestrutura. O endpoint `POST /api/leads/{id}/whatsapp-template` valida acesso ao lead, telefone do contato, template ativo da mesma loja, chama a Cloud API e registra uma mensagem `OUTBOUND` do tipo `TEMPLATE` com status inicial `SENT` ou `FAILED`. O envio de texto livre e implementado por `WhatsAppTextSenderService` e pelo endpoint `POST /api/conversations/{id}/messages`, limitado a conversas com mensagem recebida do cliente nos ultimos 24 horas. O retorno bruto do provedor fica em `conversation_messages.raw_payload` e o id externo, quando retornado, fica em `external_message_id`. Eventos de status recebidos pelo webhook atualizam a mensagem enviada correspondente pelo id externo e devem manter dados de status da Meta para rastreio tecnico.

Midias de WhatsApp devem ser armazenadas em S3 ou bucket equivalente. O banco deve guardar metadados e referencia do arquivo armazenado, nao depender de payload bruto como unico registro da midia.

No MVP de `EAI-012`, o contrato de storage fica na aplicacao por meio de uma porta de midia e a infraestrutura fornece adapter local/dev configurado por `eai.media.storage.provider=local` e `eai.media.storage.local-directory`. Esse adapter prepara a troca futura por S3 ou bucket equivalente sem acoplar casos de uso a SDK de provedor. Downloads passam sempre por endpoint autenticado, sem expor caminho local.

Configuracoes de envio:

- `META_WHATSAPP_PHONE_NUMBER_ID`
- `META_WHATSAPP_ACCESS_TOKEN`
- `META_WHATSAPP_GRAPH_API_VERSION`, com padrao local `v25.0`

Configuracoes de storage de midia:

- `EAI_MEDIA_STORAGE_PROVIDER`, com padrao local `local`
- `EAI_MEDIA_STORAGE_LOCAL_DIRECTORY`, com padrao local `.run-logs/media-storage`

Endpoints de consulta:

- `GET /api/conversations`
- `GET /api/conversations/{id}`
- `GET /api/conversations/{id}/messages`
- `POST /api/conversations/{id}/media`
- `GET /api/conversations/{conversationId}/messages/{messageId}/media`
- `GET /api/leads/{id}/conversation-messages`

## Regras de Dependencia

Dependencias permitidas:

- `api` pode depender de `application` e `domain`.
- `application` pode depender de `domain`.
- `infrastructure` pode depender de `application` e `domain`.
- `domain` depende apenas da linguagem Java e biblioteca padrao, salvo aprovacao explicita.

Dependencias proibidas:

- `domain` nao deve depender de `api`.
- `domain` nao deve depender de `infrastructure`.
- `domain` nao deve depender de Spring ou JPA.
- `application` nao deve depender de `api`.
- `api` nao deve depender de entidades JPA.

## Configuracoes Administrativas

Configuracoes administrativas sao centralizadas por meio de `com.eai.application.settings` e `com.eai.api.settings`. O modulo agrega dados existentes de empresa, loja, distribuicao, SLA, usuarios, templates, contas de e-mail e preferencias de sistema sem duplicar as regras de negocio subjacentes de cada modulo.

Endpoints de configuracao:

- `GET /api/settings`
- `GET /api/settings?companyId={companyId}&storeId={storeId}`
- `PUT /api/settings/company`
- `PUT /api/settings/store`
- `PUT /api/settings/distribution`
- `PUT /api/settings/sla`

`ADMIN` pode acessar todas as configuracoes administrativas e atualizar configuracoes de empresa. `MANAGER` pode acessar e atualizar configuracoes escopadas por loja dentro do escopo permitido. `SELLER` nao pode acessar a central administrativa de configuracoes.

## Ports and Adapters

Portas sao interfaces pertencentes a camada de aplicacao.

Exemplos atuais de portas:

- Repositorios usados por casos de uso.
- Hash de senha.
- Geracao de tokens.
- Leitura de e-mail.
- Parsing de e-mail.
- Criptografia.

Adapters vivem em infraestrutura e implementam portas.

Exemplos de adapters:

- Adapters de persistencia Spring Data.
- Provedor de token JWT.
- Hasher de senha BCrypt.
- Leitor de e-mail IMAP.
- Implementacao de criptografia de desenvolvimento.

## Fluxo de Requisicao

Fluxo tipico de comando:

1. A requisicao HTTP chega a um controller da API.
2. O controller valida o DTO de entrada.
3. O controller mapeia a entrada para um comando de aplicacao.
4. O servico de aplicacao carrega objetos de dominio por meio de portas.
5. O objeto de dominio reforca invariantes locais.
6. O servico de aplicacao coordena persistencia e efeitos colaterais.
7. O adapter de infraestrutura persiste estado ou chama servicos externos.
8. O controller mapeia o resultado para DTO de resposta.

Fluxo tipico de consulta:

1. A requisicao HTTP chega a um controller da API.
2. O controller extrai query parameters.
3. O controller delega para o servico de consulta da aplicacao.
4. O servico de aplicacao aplica autorizacao e escopo de tenant.
5. A porta de repositorio retorna dados de dominio ou read-model.
6. O controller mapeia o resultado para DTO de resposta.

## Arquitetura do Frontend

O frontend e uma aplicacao Vite React usando TypeScript e Material UI.

Estrutura atual:

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

Responsabilidades:

- `app`: bootstrap da aplicacao, router e query client.
- `pages`: telas de rota.
- `features`: componentes, hooks, schemas e helpers especificos de feature quando extraidos.
- `components`: componentes de UI reutilizaveis e compartilhados.
- `hooks`: hooks React compartilhados.
- `layouts`: componentes de layout.
- `services`: clientes de API e modulos de servico HTTP.
- `theme`: tema do Material UI.
- `types`: tipos TypeScript compartilhados.

Navegacao autenticada:

- O layout autenticado usa menu lateral com Dashboard, Leads, Pipeline, Agenda, Conversas, Relatorios, Atrasados, Usuarios, Empresas, Lojas, Templates, E-mails e Configuracoes.
- Empresas e visivel apenas para `ADMIN`.
- Relatorios gerenciais completos ficam para segunda fase.
- Atrasados, Lojas, Usuarios, Templates, E-mails e Configuracoes sao visiveis para `ADMIN` e `MANAGER`.
- Criacao de usuarios e vinculo de tenant ficam disponiveis apenas para `ADMIN`.

Telas principais:

- A tela de Leads fica disponivel em `/leads`.
- A tela de Leads oferece cards de status e SLA em estilo CRM, filtros, tabela paginada, criacao de lead, drawer de detalhe do lead, chips de status, chips de origem, atribuicao rapida, atribuicao automatica, distribuicao de pendentes, criacao/conclusao de follow-up, notas, tags e timeline de historico.
- O Kanban do pipeline fica em `/pipeline`.
- A agenda de follow-ups fica em `/follow-ups`.
- A tela inicial de conversas fica em `/conversations`.
- A fila de atrasados fica em `/leads/overdue`.
- A central administrativa de configuracoes fica em `/settings`.

Configuracoes no frontend:

- A tela de configuracoes possui abas para Empresa, Loja, Usuarios, Distribuicao, SLA, Templates, E-mail e Sistema.
- Formularios editaveis usam validacao client-side e chamam endpoints agregados de configuracao para mudancas de empresa, loja, distribuicao e SLA.

Regras:

- Usar TypeScript.
- Usar Material UI para primitivas de UI e consistencia de tema.
- Usar React Router para navegacao.
- Usar React Query para estado de servidor.
- Usar Axios por meio de modulos em `services`.
- Usar React Hook Form e Zod quando formularios forem introduzidos ou modificados.
- Evitar chamadas diretas de API em page components quando pratico.

Divida tecnica atual:

- `frontend/src/pages/LeadsPage.tsx` e grande e deve ser dividido em modulos de feature antes de mudancas relevantes no workflow de leads.
- `frontend/src/hooks/useAuth.tsx` gera warning de lint do React Fast Refresh porque exporta provider e hook no mesmo arquivo.

## Arquitetura de Infraestrutura

Infraestrutura local atual:

- Docker Compose inicia PostgreSQL.
- Backend e frontend podem rodar localmente com Java/Node instalados.
- Em ambientes sem Java/Node, backend e frontend podem ser executados manualmente via Docker.

Divida tecnica atual:

- `docker-compose.yml` nao define servicos de backend ou frontend.
- `docker/` e `scripts/` possuem apenas placeholders.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Docker Compose deve ser dono da stack local completa, incluindo backend e frontend?
- Deploy de producao deve usar imagens Docker construidas a partir de Dockerfiles do repositorio?
- Scripts devem ser adicionados para comandos comuns de desenvolvimento?

## Arquitetura de Seguranca

Estado atual:

- Spring Security protege rotas do backend.
- Access tokens JWT sao usados para autenticacao da API.
- Refresh tokens sao persistidos.
- Frontend armazena tokens no browser storage por meio de `tokenStorage`.
- Producao exige `JWT_SECRET`.

Riscos conhecidos:

- Armazenamento de tokens em `localStorage` e conveniente, mas aumenta exposicao a roubo de tokens por XSS.
- A criptografia de senha de e-mail atualmente usa uma implementacao Base64 de desenvolvimento e nao e adequada para producao.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Refresh tokens devem migrar para cookies HttpOnly?
- Qual e a estrategia de criptografia de producao para credenciais IMAP?
- Qual e a politica de CORS para producao?

## Arquitetura de Testes

Estado atual:

- Testes de backend usam JUnit e suporte de testes do Spring Boot.
- Testes unitarios e de API leves usam H2 no profile de teste em modo de compatibilidade com PostgreSQL.
- Testes de integracao backend com sufixo `*IT` ou `*IntegrationTest` rodam pelo Maven Failsafe contra PostgreSQL real via Testcontainers.
- Frontend possui checks de build e lint, mas nenhum test runner configurado.

Regras:

- Adicionar testes quando comportamento de negocio ou logica compartilhada mudar.
- Preferir testes unitarios focados para dominio e aplicacao.
- Usar testes de integracao quando o comportamento depender de Spring, persistencia ou fronteiras HTTP.
- Usar Testcontainers/PostgreSQL para smoke de migrations Flyway, constraints reais e fluxos HTTP criticos.
- Todo teste backend com JUnit deve ter `@DisplayName` em PT-BR descrevendo o comportamento validado.
- Nao pular testes falhando sem documentar o motivo.

Status:
PARCIALMENTE DEFINIDO

Perguntas para o Software Architect:

- Qual stack de testes frontend deve ser adotada?

## Decisoes Arquiteturais

ADRs iniciais:

- `docs/tecnico/adr/0001-use-hexagonal-architecture.md`
- `docs/tecnico/adr/0002-flyway-owns-database-schema.md`
- `docs/tecnico/adr/0003-separate-api-domain-and-persistence-models.md`
- `docs/tecnico/adr/0004-use-jwt-access-tokens-and-persisted-refresh-tokens.md`

Decisoes arquiteturais futuras devem ser registradas como ADRs.
