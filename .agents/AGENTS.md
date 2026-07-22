# Onboarding De Agentes EAI

Este diretorio e o guia de onboarding para agentes de IA trabalhando no EAI.

## Fonte Oficial Da Verdade

Leia os documentos nesta ordem antes de implementar qualquer mudanca:

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
14. ADRs relevantes em `docs/tecnico/adr/`
15. Arquivo especifico do papel em `.agents/`

Quando documentacao e codigo entrarem em conflito, nao escolha um lado silenciosamente. Registre o conflito no resumo do trabalho ou na documentacao apropriada.

## Acordo De Trabalho

- Analise o repositorio antes de alterar arquivos.
- Prefira padroes existentes.
- Mantenha mudancas pequenas e focadas.
- Nao implemente comportamento de negocio sem aprovacao documentada do Product Owner.
- Nao invente regras para preencher lacunas.
- Registre informacoes funcionais ausentes como pendencia para o Product Owner.
- Centralize duvidas de produto em `docs/negocio/pendencias.md`.
- Respeite a arquitetura hexagonal.
- Mantenha controllers livres de regras de negocio.
- Nao exponha entidades de persistencia em respostas da API.
- Nao modifique migrations Flyway existentes depois que forem aplicadas fora de experimentacao local.
- Sempre reporte as validacoes executadas.
- Sempre reporte vulnerabilidades apontadas por build, auditoria de dependencias, Mend/SCA ou alertas de CVE do Java.
- Antes de aprovar, mergear ou concluir uma PR, faca Code Review objetivo do diff contra `main`: escopo do card, aderencia a arquitetura, testes, validacoes, vulnerabilidades/warnings e impactos no handoff.
- Em todo card, registre achados e debitos tecnicos percebidos durante implementacao, validacao ou review; se nao forem tratados no escopo, comente no Trello e sugira card futuro quando aplicavel.
- Quando CI/checks estiverem rodando, use o tempo para trabalho sem conflito no mesmo workspace: revisar diff, preparar Code Review, consultar logs, atualizar Trello, registrar debitos ou planejar proximos cards. Nao misture implementacao de outro card na mesma working tree.
- Em cards que envolvam testes unitarios novos ou alterados, use `@DisplayName` em PT-BR descrevendo o comportamento validado, salvo quando a tecnologia do teste nao suportar equivalente.
- Em todo card, avalie e registre se surgiram oportunidades de gates de qualidade: coverage minimo, code smell/static analysis, Mend/SCA, auditoria de imagens ou politicas equivalentes.
- Use `docs/tecnico/contexto-atual.md` como handoff operacional entre pessoas e agentes.
- Quando houver mais de um dev/agente trabalhando em paralelo, registre cards e branches em andamento por responsavel em `docs/tecnico/contexto-atual.md`; nao substitua o trabalho ativo de outra pessoa por um unico "card em andamento" global.
- Use o Trello como lock operacional imediato: antes de puxar card, consulte o board; se o card estiver `Em andamento` ou atribuido a outra pessoa, nao assuma pelo que esta na `main`. O handoff versionado pode estar atrasado ate o PR ser mergeado.
- Evite registrar status transitorio de Code Review como trabalho ativo no handoff. Para PR aberta, referencie a PR e use o Trello como fonte operacional imediata.
- Evite commits que reiniciam CI apenas para trocar status transitorio no handoff, como `em andamento` para `concluido`. Registre conclusao e merge no Trello; atualize `docs/tecnico/contexto-atual.md` no proximo commit funcional/documental ou em batch de manutencao quando nao gerar rodada extra desnecessaria.
- Ao resolver conflito de merge em `docs/tecnico/contexto-atual.md`, reconcilie os dois lados: preserve o estado ja presente na `main` e mantenha o estado da branch sendo integrada.
- Use cards `EAI-###` do Trello para nomear branches, PRs e referencias de commits.
- Toda mudanca deve seguir o fluxo de Trello, branch e PR descrito em `docs/tecnico/trello-workflow.md`, inclusive documentacao, processo e ajustes pequenos. Nao commite direto na `main`.
- Todo novo problema, warning, risco, melhoria ou item identificado deve ser registrado no Trello, como card novo ou comentario em card existente, antes de decidir se sera tratado agora ou apenas mantido no backlog.
- Atribua membro ao card somente quando ele for efetivamente puxado para execucao pelo responsavel. Cards em backlog ou apenas comentados/triados devem permanecer sem membro, salvo historico de execucao real anterior.

## Fluxo De Desenvolvimento

1. Leia a documentacao oficial relevante.
2. Confirme o card `EAI-###` no Trello e a branch correspondente.
3. Inspecione o codigo existente na area afetada.
4. Identifique lacunas de regra de negocio antes de codar.
5. Peca esclarecimento ao Product Owner quando o comportamento nao estiver definido.
6. Implemente apenas comportamento aprovado.
7. Adicione ou atualize testes quando comportamento ou logica compartilhada mudar.
8. Para testes unitarios novos ou alterados, use `@DisplayName` em PT-BR quando a stack suportar.
9. Atualize documentacao quando arquitetura, API, setup, workflow ou regras de produto mudarem.
10. Execute validacoes relevantes.
11. Enquanto CI/checks rodam, avance apenas tarefas sem conflito no mesmo workspace.
12. Antes de mergear ou concluir, revise o diff da PR contra `main` e registre achados de Code Review.
13. Atualize Trello quando o status operacional mudar. Atualize `docs/tecnico/contexto-atual.md` quando a informacao precisar ficar versionada e evite commit isolado que rode CI apenas por status transitorio.
14. Resuma mudancas, validacoes, vulnerabilidades, riscos, debitos tecnicos e pendencias.

Este fluxo tambem vale para mudancas somente documentais ou de processo. Nao ha excecao para commit direto na `main`.

## Padrao De Commit

Use conventional commits:

- `feat: ...` para nova funcionalidade visivel ao usuario.
- `fix: ...` para correcao de bug.
- `chore: ...` para tooling, setup ou manutencao.
- `docs: ...` para mudancas apenas de documentacao.
- `test: ...` para mudancas de testes.
- `refactor: ...` para mudancas sem alteracao de comportamento.

## Definicao De Pronto

Uma tarefa so esta concluida quando:

- O codigo compila quando codigo e alterado.
- Testes relevantes passam ou testes ausentes sao documentados explicitamente.
- Vulnerabilidades conhecidas do build, dependencias, imagens e runtime Java foram corrigidas ou registradas com severidade, origem e proximo passo.
- Novo comportamento e documentado quando necessario.
- Limites de arquitetura sao respeitados.
- Nenhuma regra de negocio fica em controllers.
- Nenhuma duplicacao desnecessaria e introduzida.
- Mudancas de configuracao sao documentadas.
- Code Review foi executado antes do merge/conclusao e achados foram registrados ou explicitamente dispensados.
- A resposta final lista as validacoes executadas.

## Nota Historica Da Fase Inicial

Na fase inicial, o repositorio era somente de documentacao. Na fase atual, implementacao e permitida apenas quando houver card `EAI-###` no Trello, decisao/documentacao oficial correspondente e branch conforme `docs/tecnico/trello-workflow.md`.
