# AGENTS.md

Ponto de entrada permanente para agentes de IA trabalhando no projeto EAI.

O onboarding oficial de agentes fica em `.agents/`.

Leia primeiro:

1. `.agents/AGENTS.md`
2. `docs/vision.md`
3. `docs/business-rules.md`
4. `docs/domain.md`
5. `docs/use-cases.md`
6. `docs/architecture.md`
7. `docs/api.md`
8. `docs/database.md`
9. `docs/roadmap.md`
10. Arquivos relevantes em `.agents/` conforme o papel executado

Regras importantes:

- Nao invente regras de negocio.
- Nao implemente comportamento que nao esteja documentado ou explicitamente aprovado.
- Registre informacoes funcionais ausentes como pendencias para o Product Owner.
- Respeite a arquitetura hexagonal.
- Mantenha controllers livres de regras de negocio.
- Mantenha o dominio independente de Spring e JPA.
- Nao modifique migrations Flyway existentes depois que forem aplicadas fora de experimentacao local.

Durante a Sprint 0, o repositorio e somente de documentacao: sem implementacao de funcionalidades, mudancas de banco, mudancas de API, alteracoes de regras de negocio ou grandes refactors.
