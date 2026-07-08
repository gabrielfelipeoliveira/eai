# Guia Do Agente Backend

## Stack

- Java 21
- Spring Boot 4
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- Lombok
- MapStruct
- OpenAPI/Swagger
- JUnit e suporte de testes do Spring Boot

## Estrutura De Pacotes

O codigo backend fica abaixo de `com.eai`:

- `domain`
- `application`
- `infrastructure`
- `api`

## Regras De Dominio

- Mantenha o dominio independente do Spring.
- Mantenha o dominio independente do JPA.
- Coloque regras e invariantes de dominio em objetos de dominio quando pertencerem a um conceito de dominio.
- Use nomes claros para entidades, metodos e transicoes de estado.
- Nao coloque detalhes HTTP ou de persistencia em classes de dominio.

## Regras De Aplicacao

- Coloque casos de uso e orquestracao em servicos de aplicacao.
- Use ports para dependencias exigidas pelos casos de uso.
- Mantenha comportamento de negocio fora de controllers.
- Nao dependa de DTOs da API.
- Centralize politicas repetidas quando a duplicacao se tornar relevante.
- Registre regras de negocio ausentes em vez de assumir comportamento.

## Regras De Infraestrutura

- Coloque entidades JPA em infraestrutura.
- Coloque repositories Spring Data em infraestrutura.
- Coloque adapters de persistencia em infraestrutura.
- Coloque clientes externos e configuracoes de framework em infraestrutura.
- Infraestrutura implementa ports da aplicacao.

## Regras De API

- Controllers lidam com preocupacoes HTTP.
- Controllers validam DTOs de request.
- Controllers mapeiam DTOs de request para commands ou entradas de consulta.
- Controllers mapeiam resultados da aplicacao para DTOs de response.
- Controllers nao devem conter regras de negocio.
- Controllers nao devem vazar entidades de persistencia.
- Enums tecnicos podem fazer parte dos contratos, mas labels para usuario devem passar pelo catalogo `GET /api/metadata`.
- Ao introduzir novo enum visivel no frontend ou app, atualize o catalogo de metadados e os tipos do cliente.

## Regras De Banco

- PostgreSQL e o banco principal.
- Flyway e dono das migrations de schema.
- Nunca altere migrations existentes depois que forem aplicadas fora de experimentacao local.
- Crie novas migrations para mudancas de schema.
- Use migrations deterministicas.
- Prefira constraints explicitas para invariantes importantes.

## Regras De Testes

- Adicione testes quando comportamento mudar.
- Prefira testes unitarios de dominio/aplicacao para logica de negocio.
- Use testes de integracao para limites Spring, API e persistencia.
- Nao ignore testes falhando sem documentar o motivo.
- Mantenha testes focados em comportamento, nao em detalhes de implementacao.

## Comandos De Validacao

Quando Java/Maven local nao estiver disponivel, Docker pode ser usado:

```bash
docker run --rm -v "$PWD/backend:/workspace" -v eai-maven-cache:/root/.m2 -w /workspace maven:3.9-eclipse-temurin-21 mvn test
```

## Checklist Do Agente Backend

- Leu a documentacao relevante primeiro?
- Identificou regras de negocio pendentes?
- Manteve controllers finos?
- Manteve dominio livre de framework?
- Evitou expor entidades de persistencia?
- Adicionou/atualizou testes quando necessario?
- Evitou modificar migrations existentes?
