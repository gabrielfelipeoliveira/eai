# Documentacao EAI

Esta pasta separa a documentacao por contexto para facilitar a responsabilidade de leitura, revisao e resposta.

## Documentos De Negocio

Use `docs/negocio/` para decisoes e contexto de produto. Esta pasta deve ser a referencia principal para Product Owner, stakeholders de negocio e agentes que estejam esclarecendo regras antes de implementar.

- [Visao do produto](negocio/vision.md)
- [Regras de negocio](negocio/business-rules.md)
- [Pendencias de produto](negocio/pendencias.md)
- [Modelo de dominio](negocio/domain.md)
- [Casos de uso](negocio/use-cases.md)
- [Roadmap](negocio/roadmap.md)

## Documentos Tecnicos

Use `docs/tecnico/` para decisoes e contexto de engenharia. Esta pasta deve ser a referencia principal para Software Architect, desenvolvedores e agentes que estejam avaliando arquitetura, API, banco, setup ou operacao tecnica.

- [Arquitetura](tecnico/architecture.md)
- [Diretrizes de API](tecnico/api.md)
- [Banco de dados](tecnico/database.md)
- [Guia de desenvolvimento](tecnico/development-guide.md)
- [Importador de leads por e-mail](tecnico/email-importer.md)
- [ADRs](tecnico/adr/)

## Regra De Uso

- Se a pergunta for sobre comportamento esperado, regra, ator, fluxo, prioridade ou escopo de produto, comece por `docs/negocio/`.
- Se a pergunta for sobre implementacao, arquitetura, infraestrutura, API, banco, setup, seguranca tecnica ou padroes de codigo, comece por `docs/tecnico/`.
- Se houver conflito entre negocio, tecnica e codigo, registre o conflito em vez de escolher um lado silenciosamente.
