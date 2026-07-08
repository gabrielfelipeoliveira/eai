# Guia Do Agente Revisor

Use este checklist para revisoes de codigo.

## Arquitetura

- A mudanca respeita a arquitetura hexagonal?
- Controllers estao limitados a orquestracao HTTP?
- Regras de negocio estao na aplicacao ou no dominio?
- O dominio permanece independente de Spring e JPA?
- Ports estao definidos na aplicacao e adapters na infraestrutura?
- Entidades de persistencia ficaram fora das respostas da API?
- DTOs estao separados dos objetos de dominio?
- As dependencias seguem as direcoes permitidas?

## Regras De Negocio

- O comportamento esta documentado em `docs/negocio/business-rules.md` ou `docs/negocio/use-cases.md`?
- Se o comportamento estava incerto, ele foi registrado como pendente em vez de assumido?
- Decisoes do Product Owner estao refletidas na documentacao?
- Conflitos entre codigo e documentacao foram destacados?

## SOLID E Clean Code

- As responsabilidades estao claras?
- A duplicacao e relevante o bastante para extrair?
- Os nomes sao precisos?
- Comentarios sao raros e uteis?
- A mudanca e focada?
- Refactors nao relacionados foram evitados?

## Backend

- As constraints de validacao sao apropriadas?
- Excecoes sao mapeadas de forma consistente?
- Limites transacionais sao intencionais?
- Ports de repository sao usados em vez de acesso direto a infraestrutura?
- Constraints de banco ou migrations sao necessarias?
- Se uma migration foi adicionada, ela e nova e deterministica?

## Frontend

- Chamadas de API sao feitas por modulos de service?
- Estado de servidor e tratado com React Query quando apropriado?
- Formularios usam React Hook Form e Zod quando apropriado?
- A UI esta consistente com Material UI e com o design existente?
- Paginas/componentes grandes foram divididos quando a mudanca aumentou a complexidade?
- Checagens de perfil sao tratadas apenas como UX, nao como seguranca backend?

## Seguranca

- Autenticacao e autorizacao sao aplicadas no backend?
- Limites de tenant estao protegidos?
- Segredos ficaram fora de codigo e exemplos de documentacao?
- Tokens e credenciais sao tratados com cuidado?
- Campos sensiveis sao excluidos das respostas?

## Performance

- Endpoints de lista sao paginados quando os dados podem crescer?
- Filtros de banco sao indexados ou as implicacoes de indice foram documentadas?
- Operacoes caras sao limitadas?
- O tamanho do bundle frontend foi afetado?

## Testes E Validacao

- Testes relevantes foram adicionados ou atualizados?
- Testes backend rodaram?
- Build/lint frontend rodou quando o frontend mudou?
- Testes ausentes foram documentados?

## Documentacao

- Documentos relevantes foram atualizados?
- Uma ADR e necessaria?
- O README precisa de atualizacoes de setup ou workflow?

## Estilo Da Saida De Review

Comece pelos achados ordenados por severidade. Inclua referencias de arquivo e linha. Se nenhum problema for encontrado, diga isso claramente e mencione risco residual ou lacunas de teste.
