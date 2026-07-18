# AGENTS.md

Ponto de entrada permanente para agentes de IA trabalhando no projeto EAI.

O onboarding oficial de agentes fica em `.agents/`.

Leia primeiro:

1. `.agents/AGENTS.md`
2. `docs/README.md`
3. `docs/tecnico/contexto-atual.md`
4. `docs/tecnico/trello-workflow.md`
5. `docs/negocio/vision.md`
6. `docs/negocio/business-rules.md`
7. `docs/negocio/pendencias.md`
8. `docs/negocio/domain.md`
9. `docs/negocio/use-cases.md`
10. `docs/negocio/roadmap.md`
11. `docs/tecnico/architecture.md`
12. `docs/tecnico/api.md`
13. `docs/tecnico/database.md`
14. Arquivos relevantes em `.agents/` conforme o papel executado

Regras importantes:

- Nao invente regras de negocio.
- Nao implemente comportamento que nao esteja documentado ou explicitamente aprovado.
- Registre informacoes funcionais ausentes como pendencias para o Product Owner.
- Centralize duvidas de produto em `docs/negocio/pendencias.md`.
- Respeite a arquitetura hexagonal.
- Mantenha controllers livres de regras de negocio.
- Mantenha o dominio independente de Spring e JPA.
- Nao modifique migrations Flyway existentes depois que forem aplicadas fora de experimentacao local.
- Sempre reporte vulnerabilidades apontadas por build, auditoria de dependencias, Mend/SCA ou alertas de CVE do Java, mesmo quando a correcao ficar fora do escopo da tarefa.
- Use `docs/tecnico/contexto-atual.md` como handoff operacional entre pessoas e agentes.
- Use cards `EAI-###` do Trello para nomear branches, PRs e referencias de commits.

Durante a Sprint 0, o repositorio e somente de documentacao: sem implementacao de funcionalidades, mudancas de banco, mudancas de API, alteracoes de regras de negocio ou grandes refactors.
