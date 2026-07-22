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
- Access tokens JWT usam `HS256`, `typ=JWT`, assinatura HMAC comparada de forma resistente a timing e claims obrigatorias `sub`, `email`, `roles` e `exp`.
- Tokens com algoritmo inesperado, assinatura invalida, expirados ou sem claims obrigatorias sao rejeitados como acesso nao autorizado.
- Refresh tokens sao persistidos.
- Requisicoes protegidas usam `Authorization: Bearer <token>`.
- Login e refresh sao publicos.
- Health check e metadados de apresentacao sao publicos. OpenAPI/Swagger e publico em perfis locais/teste, mas fica desabilitado por padrao em producao.
- Usuario inativo deve receber mensagem generica.
- Login em multiplas sessoes nao deve ser permitido.
- Deve existir no maximo uma sessao ativa por usuario.
- Rotacao de refresh token revoga imediatamente o token anterior.
- Sessao dura 30 dias.
- Logout revoga todas as sessoes do usuario.
- Desativacao de usuario revoga sessoes ativas.

Status:
PARCIALMENTE DEFINIDO

Perguntas para o Software Architect:

- Refresh tokens devem continuar no corpo da resposta ou migrar para cookies HttpOnly?
- Swagger UI e OpenAPI devem permanecer desabilitados por padrao em producao e so podem ser reabilitados por configuracao explicita de ambiente.

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
- Busca textual de leads deve ser normalizada.
- Ordenacao padrao da listagem de leads deve ser por chegada.
- A listagem de leads aplica escopo antes da paginacao: `ADMIN` visualiza todos os leads, `MANAGER` visualiza leads da empresa, `STORE_MANAGER` visualiza leads da loja e `SELLER` visualiza leads disponiveis sem dono ou sob sua responsabilidade.
- Filtros obrigatorios de tela nao sao relevantes para o MVP neste momento.

Status:
DEFINIDO PARA LEADS

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
- A suite backend valida automaticamente em perfil de teste/local que `/v3/api-docs` e publico, possui metadados e security scheme esperados, cobre os endpoints Spring MVC em `com.eai.api` e nao gera `operationId` duplicado.
- Em producao, `SPRINGDOC_API_DOCS_ENABLED` e `SPRINGDOC_SWAGGER_UI_ENABLED` ficam `false` por padrao.

Status:
PARCIALMENTE DEFINIDO

Perguntas para o Software Architect:

- O OpenAPI deve ser gerado somente a partir dos controllers ou enriquecido com anotacoes explicitas?
- Exemplos de API devem ser mantidos manualmente?

## Leads

Contrato atual de `POST /api/leads` e `PUT /api/leads/{id}`:

- `customerPhone` aceita E.164 ou telefone brasileiro com DDD; o backend persiste em E.164.
- `additionalPhones` aceita lista opcional de telefones adicionais; o backend normaliza para E.164, remove repeticoes e ignora o telefone principal quando repetido.
- `saleCurrency` e opcional e usa `BRL` quando ausente ou em branco.
- `vehicleInterest` continua como texto legado/fallback.
- `item` e opcional. No MVP, quando informado, pode conter `vehicle` como detalhe estruturado do item.
- O Lead referencia somente `itemId`; `vehicle` nao e relacionamento direto do Lead.
- Se existir lead anterior na mesma loja com qualquer telefone informado, o novo lead pode retornar `status: "DUPLICATED"` e `relatedLeadId` apontando para o lead anterior mais recente.

Contratos atuais de observacoes, historico e tags:

- `POST /api/leads/{id}/notes`: cria observacao editavel para o lead e registra evento no historico com descricao `Observacao criada`.
- `PUT /api/leads/{id}/notes/{noteId}`: edita observacao existente do lead e registra evento no historico com descricao `Observacao atualizada`.
- `GET /api/leads/{id}/notes`: lista observacoes do lead por atualizacao/criacao mais recente.
- `GET /api/leads/{id}/history`: lista historico do lead por data mais recente.
- `GET /api/leads/tags/catalog`: lista tags globais ativas cadastradas.
- `POST /api/leads/tags/catalog`: cadastra tag global com `name` e `type`.
- `POST /api/leads/{id}/tags`: associa tag global ao lead usando `tagId`.
- `GET /api/leads/{id}/tags`: lista tags associadas ao lead.
- `DELETE /api/leads/{id}/tags/{tagId}`: remove associacao da tag com o lead.

Regras de contrato:

- Tags de lead nao sao texto livre; a associacao deve usar tag cadastrada no catalogo global.
- O backend bloqueia tag duplicada no mesmo lead.
- O backend bloqueia mais de uma tag do mesmo `type` no mesmo lead.
- Respostas de tag do lead retornam `id`, `leadId`, `tagId`, `name` e `type`.
- Respostas de observacao retornam `id`, `leadId`, `userId`, `note`, `createdAt` e `updatedAt`.

Exemplo:

```json
{
  "customerPhone": "11999990000",
  "additionalPhones": ["+5511988880000"],
  "item": {
    "name": "Anuncio Civic",
    "vehicle": {
      "name": "Honda Civic",
      "year": 2021,
      "model": "Touring",
      "value": 128900.00
    }
  }
}
```

## LGPD

Endpoints atuais, protegidos por `ADMIN`:

- `POST /api/lgpd-requests`
- `GET /api/lgpd-requests`
- `GET /api/lgpd-requests/{id}`
- `POST /api/lgpd-requests/{id}/actions`

Contrato de criacao:

- `companyId` obrigatorio.
- `storeId` opcional.
- `leadId` opcional.
- `dataSubjectName` obrigatorio.
- `dataSubjectPhone` opcional.
- `dataSubjectEmail` opcional.
- `requestType` obrigatorio: `ACCESS`, `CORRECTION`, `BLOCK`, `ANONYMIZATION` ou `DELETION`.
- `description` obrigatorio.

Contrato de acao manual:

- `actionType` obrigatorio: `ACCESS`, `CORRECTION`, `BLOCK`, `ANONYMIZATION` ou `DELETION`.
- `resolution` obrigatorio.
- `finalStatus` opcional; quando informado, aceita apenas `COMPLETED` ou `REJECTED`.

Regras do MVP:

- Apenas `ADMIN` pode criar, listar, detalhar e registrar tratamento LGPD.
- Registrar acao LGPD documenta o tratamento manual, executor e data.
- Registrar acao nao anonimiza, elimina, corrige, bloqueia ou altera automaticamente leads, conversas, mensagens, usuarios ou midias.
- Acao sem `finalStatus` move a solicitacao para `IN_PROGRESS`.
- Acao com `finalStatus` fecha a solicitacao como `COMPLETED` ou `REJECTED`.
- `GET /api/lgpd-requests` aceita filtros opcionais `status`, `companyId`, `storeId`, `leadId`, `page` e `size`.

## Conversas De WhatsApp

Endpoints atuais:

- `GET /api/conversations`
- `GET /api/conversations/{id}`
- `GET /api/conversations/{id}/messages`
- `POST /api/conversations/{id}/messages`
- `POST /api/conversations/{id}/media`
- `GET /api/conversations/{conversationId}/messages/{messageId}/media`
- `GET /api/leads/{id}/conversation-messages`
- `POST /api/leads/{id}/whatsapp-template`
- `GET /api/webhooks/whatsapp`
- `POST /api/webhooks/whatsapp`

Convencoes:

- O webhook de WhatsApp e publico para validacao e recebimento da Meta.
- `POST /api/webhooks/whatsapp` exige assinatura valida no header `X-Hub-Signature-256`, no formato `sha256=<hmac>`, calculada pela Meta sobre o corpo bruto da requisicao usando `META_WHATSAPP_APP_SECRET`.
- Requisicoes de webhook WhatsApp sem assinatura, com assinatura malformada ou divergente retornam erro de autenticacao sem processar o payload.
- Consultas de conversas exigem autenticacao e seguem escopo de tenant.
- `GET /api/conversations` retorna um resumo operacional com ids da conversa, lead/contato, vendedor responsavel, nome do lead ou contato, telefone, ultima mensagem, data/hora da ultima interacao e quantidade de mensagens nao lidas.
- `GET /api/conversations` aceita filtros opcionais `sellerId`, `messageStatus`, `startAt` e `endAt`. `messageStatus` filtra pelo status da ultima mensagem enquanto o dominio nao possuir status proprio de conversa.
- `GET /api/conversations/{id}/messages` e `GET /api/leads/{id}/conversation-messages` retornam mensagens em ordem cronologica e marcam mensagens recebidas pendentes como lidas.
- `GET /api/conversations/{id}`, `GET /api/conversations/{id}/messages` e `GET /api/leads/{id}/conversation-messages` registram acesso de `ADMIN` e `MANAGER` para auditoria.
- `POST /api/conversations/{id}/messages` envia texto livre pela WhatsApp Cloud API quando a conversa possui mensagem recebida do cliente nos ultimos 24 horas.
- `POST /api/conversations/{id}/media` envia midia por `multipart/form-data` com campo `file` e `caption` opcional, usando a mesma janela de 24 horas do texto livre.
- `POST /api/conversations/{id}/media` rejeita arquivo vazio, MIME type fora da lista permitida e arquivo acima do limite configurado antes de carregar o conteudo em memoria para envio.
- `GET /api/conversations/{conversationId}/messages/{messageId}/media` baixa a midia armazenada de forma autenticada, respeitando o mesmo escopo de acesso da conversa.
- `GET /api/conversations/{conversationId}/messages/{messageId}/media` valida MIME type e tamanho da midia armazenada antes de responder o arquivo.
- Fora da janela de 24 horas, `POST /api/conversations/{id}/messages` retorna erro de negocio `WHATSAPP_FREE_TEXT_WINDOW_EXPIRED` e o cliente deve orientar uso de template aprovado.
- O disparo de template exige autenticacao e acesso ao lead.
- O disparo de template usa template ativo e aprovado na Meta, especifico da loja do lead ou global da empresa do lead.
- O disparo envia para a WhatsApp Cloud API o `name` tecnico exatamente como aprovado na Meta e o `languageCode` do request, do template ou `pt-BR` como default.
- Eventos de status recebidos pelo webhook atualizam mensagens enviadas pelo `externalMessageId` retornado pela Meta.
- Dados de status de mensagem recebidos da Meta devem ser salvos pelo sistema.
- Midias de WhatsApp devem ser armazenadas em S3 ou bucket equivalente, com metadados/referencia persistidos.
- Limites padrao para midias WhatsApp seguem o suporte da Cloud API para os tipos usados pelo MVP: imagem 5 MB, audio 16 MB e documento 100 MB.
- `ConversationMessageResponse` retorna campos nullable de midia armazenada: `mediaStorageProvider`, `mediaStorageKey`, `mediaFileName`, `mediaSizeBytes` e `mediaSha256`.
- Placeholders/componentes de templates da Meta devem ser preenchidos automaticamente com dados disponiveis.
- O idioma padrao para template sem `languageCode` e `pt-BR`.
- O nome do template no EAI deve ser exatamente o nome aprovado na Meta.
- Templates expostos por `GET /api/templates` e `GET /api/templates/active` ignoram registros com exclusao logica.
- `GET /api/templates/active` retorna apenas templates `active=true` com `metaStatus=APPROVED`.
- DTOs de conversa e mensagem nao expoem entidades de persistencia.
- Direcao, tipo e status usam codigos tecnicos expostos tambem por `GET /api/metadata`.

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Software Architect:

- O OpenAPI deve ser gerado somente a partir dos controllers ou enriquecido com anotacoes explicitas?
- Exemplos de API devem ser mantidos manualmente?

## Notificacoes

Endpoints protegidos:

- `GET /api/notifications?unreadOnly=true&limit=20`
- `GET /api/notifications/unread-count`
- `POST /api/notifications/{id}/read`
- `POST /api/notifications/read-all`

Cada usuario autenticado acessa apenas as proprias notificacoes. O uso inicial cria notificacoes para `ADMIN` ativo quando uma conta de e-mail falha em teste de conexao ou importacao.
