# Onboarding De Agentes EAI

Este diretorio e o guia de onboarding para agentes de IA trabalhando no EAI.

## Fonte Oficial Da Verdade

Leia os documentos nesta ordem antes de implementar qualquer mudanca:

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
11. ADRs relevantes em `docs/tecnico/adr/`
12. Arquivo especifico do papel em `.agents/`

Quando documentacao e codigo entrarem em conflito, nao escolha um lado silenciosamente. Registre o conflito no resumo do trabalho ou na documentacao apropriada.

## Acordo De Trabalho

- Analise o repositorio antes de alterar arquivos.
- Prefira padroes existentes.
- Mantenha mudancas pequenas e focadas.
- Nao implemente comportamento de negocio sem aprovacao documentada do Product Owner.
- Nao invente regras para preencher lacunas.
- Registre informacoes funcionais ausentes como pendencia para o Product Owner.
- Respeite a arquitetura hexagonal.
- Mantenha controllers livres de regras de negocio.
- Nao exponha entidades de persistencia em respostas da API.
- Nao modifique migrations Flyway existentes depois que forem aplicadas fora de experimentacao local.
- Sempre reporte as validacoes executadas.

## Fluxo De Desenvolvimento

1. Leia a documentacao oficial relevante.
2. Inspecione o codigo existente na area afetada.
3. Identifique lacunas de regra de negocio antes de codar.
4. Peca esclarecimento ao Product Owner quando o comportamento nao estiver definido.
5. Implemente apenas comportamento aprovado.
6. Adicione ou atualize testes quando comportamento ou logica compartilhada mudar.
7. Atualize documentacao quando arquitetura, API, setup, workflow ou regras de produto mudarem.
8. Execute validacoes relevantes.
9. Resuma mudancas, validacoes, riscos e pendencias.

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
- Novo comportamento e documentado quando necessario.
- Limites de arquitetura sao respeitados.
- Nenhuma regra de negocio fica em controllers.
- Nenhuma duplicacao desnecessaria e introduzida.
- Mudancas de configuracao sao documentadas.
- A resposta final lista as validacoes executadas.

## Regra Da Sprint 0

A Sprint 0 e somente de documentacao. Implementacao de funcionalidades, alteracoes de API, alteracoes de banco de dados e mudancas de regras de negocio nao sao permitidas.
