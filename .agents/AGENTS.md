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
- Use `docs/tecnico/contexto-atual.md` como handoff operacional entre pessoas e agentes.
- Use cards `EAI-###` do Trello para nomear branches, PRs e referencias de commits.

## Fluxo De Desenvolvimento

1. Leia a documentacao oficial relevante.
2. Confirme o card `EAI-###` no Trello e a branch correspondente.
3. Inspecione o codigo existente na area afetada.
4. Identifique lacunas de regra de negocio antes de codar.
5. Peca esclarecimento ao Product Owner quando o comportamento nao estiver definido.
6. Implemente apenas comportamento aprovado.
7. Adicione ou atualize testes quando comportamento ou logica compartilhada mudar.
8. Atualize documentacao quando arquitetura, API, setup, workflow ou regras de produto mudarem.
9. Execute validacoes relevantes.
10. Atualize Trello e `docs/tecnico/contexto-atual.md` quando o status operacional mudar.
11. Resuma mudancas, validacoes, vulnerabilidades, riscos e pendencias.

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
- A resposta final lista as validacoes executadas.

## Regra Da Sprint 0

A Sprint 0 e somente de documentacao. Implementacao de funcionalidades, alteracoes de API, alteracoes de banco de dados e mudancas de regras de negocio nao sao permitidas.
