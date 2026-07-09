# Diretrizes de API

Este documento define convencoes de API. Ele nao define endpoints de funcionalidades. Contratos funcionais devem vir de casos de uso aprovados e da documentacao OpenAPI.

## Estilo

- Usar semantica REST.
- Usar JSON em requisicoes e respostas.
- Usar UTF-8.
- Usar nomes de recursos no plural quando representarem colecoes.
- Usar parametros de caminho para identidade de recursos.
- Usar query parameters para filtros, ordenacao, paginacao e seletores opcionais.
- Usar DTOs de requisicao para entrada da API.
- Usar DTOs de resposta para saida da API.
- Nao expor entidades de persistencia nas respostas da API.

## Versionamento

Estado atual:

- A API ativa usa `/api` como caminho base.
- Ainda nao existe uma politica formal de versionamento.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- APIs futuras devem usar versionamento por URI, como `/api/v1`?
- Qual politica de compatibilidade deve ser usada para consumidores existentes?
- Como deprecacoes devem ser comunicadas?

## Autenticacao

Padrao conhecido:

- A autenticacao usa access tokens JWT.
- Refresh tokens sao persistidos.
- Requisicoes protegidas usam `Authorization: Bearer <token>`.
- Login e refresh sao publicos.
- Health check, OpenAPI e metadados de apresentacao sao publicos.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Refresh tokens devem continuar no corpo da resposta ou migrar para cookies HttpOnly?
- O TTL dos tokens deve variar por ambiente ou papel?
- O Swagger UI deve continuar publico em producao?

## Metadados De Apresentacao

Endpoint atual:

- `GET /api/metadata`

Objetivo:

- Fornecer catalogos de apresentacao para codigos tecnicos usados por dominio, API e banco.
- Evitar que frontend web, app mobile ou outros clientes exibam enums crus como `FIRST_CONTACT`, `ROUND_ROBIN`, `SUCCESS` ou `NEW` para usuarios finais.

Contrato:

- O endpoint e publico e nao exige token.
- O cliente pode enviar `Accept-Language`.
- A localizacao suportada atualmente e `pt-BR`.
- Cada item retorna `code`, `labelKey`, `label`, `order` e `color`.
- `code` e o valor tecnico usado nas APIs de negocio.
- `labelKey` e a chave estavel para futura internacionalizacao.
- `label` e o texto ja localizado para apresentacao.
- `order` orienta ordenacao visual.
- `color` orienta componentes visuais como chips e badges.
- O catalogo inclui codigos de mensagens de conversa: direcao, tipo e status.

Convencoes:

- APIs de funcionalidades continuam aceitando e retornando codigos tecnicos.
- Clientes nao devem renderizar `code` diretamente para usuarios finais.
- Clientes devem mapear `code` para `label` usando o catalogo de metadados.
- Mudancas em labels nao devem alterar codigos tecnicos nem migrations.
- Novos enums visiveis para usuarios devem ser adicionados ao catalogo de metadados.
- Regras de negocio nao devem ser colocadas no endpoint de metadados; ele e apenas apresentacao.

Cache recomendado:

- O catalogo muda pouco e pode ser cacheado por cliente.
- O frontend web usa React Query com `staleTime` de 24 horas e cache por locale.
- Aplicativos mobile devem carregar no inicio da sessao ou no primeiro uso e persistir cache local quando fizer sentido.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais idiomas devem ser suportados na primeira versao do app?
- As labels oficiais devem usar acentos em todos os textos visiveis ao usuario?

Perguntas para o Software Architect:

- O catalogo deve evoluir para arquivos de mensagens do Spring, tabela no banco ou servico dedicado de internacionalizacao?
- Deve existir versao/ETag para invalidacao de cache de metadados?

## Autorizacao

- A autorizacao deve ser aplicada no backend.
- Visibilidade de rotas no frontend e apenas conveniencia de UX, nao seguranca.
- Escopo de tenant deve ser validado no backend.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- As regras de autorizacao devem ser centralizadas em politicas de aplicacao?
- Anotacoes de metodo da API devem continuar sendo o principal ponto de controle de acesso?

## Paginacao

Padrao conhecido:

- Endpoints de colecao que podem crescer devem ser paginados.
- A paginacao atual usa `page` zero-based e `size` positivo.
- O backend atualmente limita a listagem de leads a tamanho maximo 100.
- Respostas paginadas incluem conteudo e metadados de paginacao.

Convencao recomendada:

- `page`: numero da pagina, iniciando em zero.
- `size`: tamanho da pagina.
- `totalElements`: total de registros.
- `totalPages`: total de paginas.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Todos os endpoints paginados devem usar o mesmo wrapper de resposta?
- Qual e o tamanho maximo global de pagina?
- Ordenacao deve usar um query parameter padronizado `sort`?

## Filtros

- Usar query parameters para filtros.
- Nomes de filtros devem seguir o vocabulario da API, nao nomes de colunas do banco.
- Filtros de data/hora devem usar ISO 8601.
- Filtros de texto devem documentar a semantica de busca quando aprovada.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais filtros sao obrigatorios para as telas do MVP?
- Busca textual deve ser exata, parcial, sem acento ou normalizada?

## Respostas de Erro

Formato conhecido atual:

- `code`
- `message`
- `timestamp`

Convencoes recomendadas:

- Usar codigos de erro estaveis.
- Nao vazar stack trace ou detalhes internos.
- Retornar erros de validacao em estrutura previsivel.
- Usar `401` para autenticacao ausente ou invalida.
- Usar `403` para usuario autenticado sem permissao.
- Usar `404` quando um recurso nao existe ou nao esta disponivel para o usuario.
- Usar `409` para conflitos.
- Usar `400` para entrada invalida.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Erros de validacao devem incluir detalhes por campo?
- Erros de conflito e autorizacao devem usar mensagens localizadas?
- Codigos de erro devem ser catalogados neste documento?

## Status HTTP

Convencoes recomendadas:

- `200 OK` para leituras e atualizacoes com corpo de resposta.
- `201 Created` para criacao de recurso quando a semantica de localizacao fizer sentido.
- `204 No Content` para deletes ou comandos sem corpo de resposta.
- `400 Bad Request` para requisicao ou entrada de negocio invalida.
- `401 Unauthorized` para acesso sem autenticacao.
- `403 Forbidden` para usuario autenticado sem permissao.
- `404 Not Found` para recurso inexistente ou inacessivel.
- `409 Conflict` para conflitos de negocio.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- Endpoints de criacao devem retornar `201` de forma consistente?
- Endpoints de delete devem retornar `204` de forma consistente?

## OpenAPI

- OpenAPI/Swagger faz parte da stack do backend.
- A documentacao de API deve ser atualizada quando contratos mudarem.
- Exemplos publicos de API nao devem usar segredos reais ou dados pessoais.

Status:
PENDENTE DE DEFINIÃ‡ÃƒO

Perguntas para o Software Architect:

- O OpenAPI deve ser gerado somente a partir dos controllers ou enriquecido com anotacoes explicitas?
- Exemplos de API devem ser mantidos manualmente?

## Conversas De WhatsApp

Endpoints atuais:

- `GET /api/conversations`
- `GET /api/conversations/{id}`
- `GET /api/conversations/{id}/messages`
- `POST /api/conversations/{id}/messages`
- `GET /api/leads/{id}/conversation-messages`
- `POST /api/leads/{id}/whatsapp-template`
- `GET /api/webhooks/whatsapp`
- `POST /api/webhooks/whatsapp`

Convencoes:

- O webhook de WhatsApp e publico para validacao e recebimento da Meta.
- Consultas de conversas exigem autenticacao e seguem escopo de tenant.
- `GET /api/conversations` retorna um resumo operacional com ids da conversa, lead/contato, vendedor responsavel, nome do lead ou contato, telefone, ultima mensagem, data/hora da ultima interacao e quantidade de mensagens nao lidas.
- `GET /api/conversations/{id}/messages` e `GET /api/leads/{id}/conversation-messages` retornam mensagens em ordem cronologica e marcam mensagens recebidas pendentes como lidas.
- `POST /api/conversations/{id}/messages` envia texto livre pela WhatsApp Cloud API quando a conversa possui mensagem recebida do cliente nos ultimos 24 horas.
- Fora da janela de 24 horas, `POST /api/conversations/{id}/messages` retorna erro de negocio `WHATSAPP_FREE_TEXT_WINDOW_EXPIRED` e o cliente deve orientar uso de template aprovado.
- O disparo de template exige autenticacao e acesso ao lead.
- O disparo de template usa o template ativo da mesma loja do lead, envia o nome do template para a WhatsApp Cloud API e registra a resposta bruta do provedor na mensagem da conversa.
- Eventos de status recebidos pelo webhook atualizam mensagens enviadas pelo `externalMessageId` retornado pela Meta.
- DTOs de conversa e mensagem nao expoem entidades de persistencia.
- Direcao, tipo e status usam codigos tecnicos expostos tambem por `GET /api/metadata`.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- O OpenAPI deve ser gerado somente a partir dos controllers ou enriquecido com anotacoes explicitas?
- Exemplos de API devem ser mantidos manualmente?
