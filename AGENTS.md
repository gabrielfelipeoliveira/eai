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
- Antes de aprovar, mergear ou concluir uma PR, faca Code Review objetivo do diff contra `main`: escopo do card, aderencia a arquitetura, testes, validacoes, vulnerabilidades/warnings e impactos no handoff.
- Em todo card, registre achados e debitos tecnicos percebidos durante implementacao, validacao ou review; se nao forem tratados no escopo, comente no Trello e sugira card futuro quando aplicavel.
- Quando CI/checks estiverem rodando, use o tempo para trabalho sem conflito no mesmo workspace: revisar diff, preparar Code Review, consultar logs, atualizar Trello, registrar debitos ou planejar proximos cards. Nao misture implementacao de outro card na mesma working tree.
- Em cards que envolvam testes unitarios novos ou alterados, use `@DisplayName` em PT-BR descrevendo o comportamento validado, salvo quando a tecnologia do teste nao suportar equivalente.
- Em todo card, avalie e registre se surgiram oportunidades de gates de qualidade: coverage minimo, code smell/static analysis, Mend/SCA, auditoria de imagens ou politicas equivalentes.
- Use `docs/tecnico/contexto-atual.md` como handoff operacional entre pessoas e agentes.
- Em trabalho paralelo, registre cards e branches em andamento por responsavel no handoff; nao trate o projeto como se houvesse apenas um card ativo global.
- Use o Trello como lock operacional imediato antes de puxar card; `contexto-atual.md` na `main` pode estar atrasado ate o PR de reserva ser mergeado.
- Evite registrar status transitorio de Code Review como trabalho ativo no handoff. Para PR aberta, referencie a PR e use o Trello como fonte operacional imediata.
- Ao resolver conflito no handoff, preserve tanto o estado ja presente na `main` quanto o estado da branch que esta sendo integrada.
- Use cards `EAI-###` do Trello para nomear branches, PRs e referencias de commits.
- Toda mudanca deve seguir o fluxo de Trello, branch e PR descrito em `docs/tecnico/trello-workflow.md`, inclusive documentacao, processo e ajustes pequenos. Nao commite direto na `main`.

Nota historica: na fase inicial, o repositorio era somente de documentacao. Na fase atual, implementacao e permitida apenas quando houver card `EAI-###` no Trello, decisao/documentacao oficial correspondente e branch conforme `docs/tecnico/trello-workflow.md`.
