# AGENTS.md

Ponto de entrada permanente para agentes de IA trabalhando no projeto EAI.

O onboarding oficial de agentes fica em `.agents/`.

Leia primeiro:

1. `.agents/AGENTS.md`
2. `docs/README.md`
3. `docs/negocio/vision.md`
4. `docs/negocio/business-rules.md`
5. `docs/negocio/domain.md`
6. `docs/negocio/use-cases.md`
7. `docs/negocio/roadmap.md`
8. `docs/tecnico/architecture.md`
9. `docs/tecnico/api.md`
10. `docs/tecnico/database.md`
11. Arquivos relevantes em `.agents/` conforme o papel executado

Regras importantes:

- Nao invente regras de negocio.
- Nao implemente comportamento que nao esteja documentado ou explicitamente aprovado.
- Registre informacoes funcionais ausentes como pendencias para o Product Owner.
- Respeite a arquitetura hexagonal.
- Mantenha controllers livres de regras de negocio.
- Mantenha o dominio independente de Spring e JPA.
- Nao modifique migrations Flyway existentes depois que forem aplicadas fora de experimentacao local.

Durante a Sprint 0, o repositorio e somente de documentacao: sem implementacao de funcionalidades, mudancas de banco, mudancas de API, alteracoes de regras de negocio ou grandes refactors.
