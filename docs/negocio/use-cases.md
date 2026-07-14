# Casos De Uso

Este documento lista os casos de uso conhecidos para o MVP confirmado. Duvidas sobre entradas, atores, regras e excecoes ficam centralizadas em [Pendencias de produto](pendencias.md).

## Autenticacao

### Login

Ator:

- Usuario cadastrado.

Entradas:

- E-mail.
- Senha.

Saidas:

- Token de acesso.
- Refresh token.
- Tipo do token.

Pre-condicoes:

- Usuario existe.
- Usuario esta ativo.
- Senha corresponde ao hash armazenado.

### Renovar Sessao

Ator:

- Usuario autenticado com refresh token.

Entradas:

- Refresh token.

Saidas:

- Novo token de acesso.
- Novo refresh token.

### Logout

Ator:

- Usuario autenticado.

Saidas:

- Sessao encerrada conforme politica de autenticacao definida.

## Tenancy E Usuarios

### Gerenciar Empresas

Ator:

- `ADMIN`.

Entradas:

- Dados da empresa.

Saidas:

- Empresa criada ou atualizada.

### Gerenciar Lojas

Ator:

- `ADMIN`.
- `MANAGER`, dentro da empresa.

Entradas:

- Dados da loja.

Saidas:

- Loja criada ou atualizada.

Pre-condicoes:

- Empresa existe.
- Ator possui permissao para a empresa.

### Gerenciar Usuarios

Ator:

- `ADMIN`.
- Papeis gerenciais conforme regra de permissao ainda pendente.

Entradas:

- Dados do usuario.
- Papel unico.
- Empresa.
- Loja quando aplicavel.

Saidas:

- Usuario criado, atualizado, ativado ou desativado.

Pre-condicoes:

- Papel informado pertence ao MVP.
- Usuario operacional possui loja quando necessario.

## Gestao De Leads

### Criar Lead

Ator:

- `ADMIN`.
- `MANAGER`.
- `STORE_MANAGER`.
- `PRE_SALES`.
- `SELLER`, quando permitido pelo fluxo operacional.

Entradas:

- Empresa.
- Loja.
- Dados do cliente.
- Veiculo de interesse.
- Origem.
- Mensagem original.
- Usuario responsavel opcional.

Saidas:

- Lead criado.
- Historico registrado.

Regras:

- Pre-venda gera o lead; depois disso, o lead aparece como disponivel no pipeline.
- Leads podem entrar por WhatsApp ou por e-mail.
- Duplicidade e avaliada por telefone/WhatsApp e loja.

### Listar E Pesquisar Leads

Ator:

- `ADMIN`.
- `MANAGER`.
- `STORE_MANAGER`.
- `SELLER`.
- `PRE_SALES`.
- `F_AND_I`, quando participar das etapas de simulacao ou proposta.

Entradas:

- Parametros de paginacao.
- Filtros opcionais.

Saidas:

- Lista paginada de leads.

Regras:

- `ADMIN` visualiza globalmente.
- `MANAGER` visualiza a empresa.
- `STORE_MANAGER` visualiza a loja.
- `SELLER` visualiza leads disponiveis e leads sob sua responsabilidade.
- Escopo de `PRE_SALES` e `F_AND_I` deve respeitar loja e etapa operacional.

### Assumir Lead Disponivel

Ator:

- `SELLER`.

Entradas:

- Identidade do lead disponivel.

Saidas:

- Lead atribuido ao vendedor.

Pre-condicoes:

- Lead esta disponivel.
- Vendedor pertence a loja do lead.

### Atribuir Lead Manualmente

Ator:

- `MANAGER`.
- `STORE_MANAGER`.

Entradas:

- Identidade do lead.
- Identidade do usuario responsavel.

Saidas:

- Lead atribuido.
- Historico registrado.

Regras:

- `MANAGER` pode atribuir leads dentro da empresa.
- `STORE_MANAGER` pode atribuir leads dentro da propria loja.
- Distribuicao automatica fica para segunda fase.

### Alterar Status Do Lead

Ator:

- Usuario com acesso ao lead.

Entradas:

- Identidade do lead.
- Novo status.
- Descricao opcional.

Saidas:

- Status atualizado.
- Historico registrado.

Regras:

- `VISIT_SCHEDULED`, `SIMULATING` e `PROPOSAL_APPROVED` sao etapas opcionais.
- `SIMULATING` e `PROPOSAL_APPROVED` ficam conceitualmente entre negociacao e proposta enviada, sem ordem obrigatoria.
- `F_AND_I` participa das etapas de simulacao e proposta.

### Registrar Duplicidade

Ator:

- Sistema.
- Usuario com acesso ao lead, quando revisao manual for necessaria.

Entradas:

- Novo lead recebido.
- Telefone/WhatsApp.
- Loja.

Saidas:

- Novo lead marcado como duplicado quando houver correspondencia.
- Historico da nova chegada registrado.
- Relacao com lead ou conversa anterior preservada.

## Pipeline

### Visualizar Pipeline

Ator:

- Usuario com acesso ao escopo.

Saidas:

- Leads agrupados por status.

Regras:

- Etapas opcionais aparecem visualmente no MVP.
- Nenhum status atual precisa ser ocultado no MVP.
- Arquitetura deve permitir etapas configuraveis em fase futura.

## WhatsApp E Conversas

### Receber Mensagem Pelo WhatsApp

Ator:

- WhatsApp Cloud API.

Entradas:

- Payload do webhook.
- Numero de WhatsApp da loja.

Saidas:

- Contato criado ou atualizado.
- Conversa criada ou atualizada.
- Mensagem registrada.

Regras:

- Cada loja deve ter apenas um numero de WhatsApp.
- Conversa pertence a loja do numero.
- Quando nao houver vendedor responsavel, a conversa fica na fila da loja.
- Quando houver lead correspondente, a conversa deve ser vinculada ao lead.

### Listar Conversas

Ator:

- `ADMIN`.
- `MANAGER`.
- `STORE_MANAGER`.
- `SELLER`.

Saidas:

- Lista de conversas do escopo do usuario.

Regras:

- `ADMIN` visualiza globalmente.
- `MANAGER` visualiza conversas da empresa.
- `STORE_MANAGER` visualiza conversas da loja.
- `SELLER` visualiza conversas dos leads sob sua responsabilidade.
- Conversas sem vendedor ficam disponiveis na fila da loja.

### Assumir Conversa Ou Lead Da Fila

Ator:

- `SELLER`.
- `MANAGER`, quando assumir o lead.
- `STORE_MANAGER`, dentro da loja.

Saidas:

- Conversa passa a ter dono responsavel.
- Lead relacionado passa a ter responsavel quando aplicavel.

Regras:

- Pre-venda assumir conversas da fila fica para segunda fase.
- Gerente pode responder uma conversa somente se assumir o lead.
- Enquanto o lead estiver no nome de um vendedor, gerente apenas supervisiona.

### Enviar Template

Ator:

- Usuario com acesso ao lead ou conversa.

Entradas:

- Identidade do lead ou conversa.
- Identidade do template.

Saidas:

- Mensagem enviada ou falha registrada.
- Comunicacao registrada.

Regras:

- Templates da empresa podem ser usados por todas as lojas da empresa.
- Templates da loja sao especificos daquela loja.

### Enviar Texto Livre

Ator:

- Dono responsavel da conversa.

Entradas:

- Identidade da conversa.
- Conteudo textual.

Saidas:

- Mensagem enviada ou falha registrada.

Pre-condicoes:

- Janela de 24 horas do WhatsApp permite texto livre.

## Importacao De Leads Por E-Mail

### Sincronizar Leads Por E-Mail

Ator:

- Usuario com permissao para a conta de e-mail.
- Scheduler, quando configurado.

Entradas:

- Conta de e-mail.
- Mensagens recebidas.

Saidas:

- Leads criados.
- Duplicidades marcadas quando aplicavel.
- Historico registrado.

Regras:

- Plataformas como Webmotors e iCarros devem ser registradas como `LeadSource`.
- Duplicidade usa telefone/WhatsApp e loja.
- Entrada duplicada gera novo lead marcado como duplicado e preserva relacao com conversa ou lead anterior.

## LGPD

### Executar Solicitacao LGPD

Ator:

- `ADMIN`.

Entradas:

- Identificacao do titular.
- Tipo de solicitacao.
- Justificativa ou base operacional.

Saidas:

- Solicitacao registrada.
- Dados acessados, corrigidos, bloqueados, anonimizados ou eliminados quando aplicavel.
- Acao executada registrada para auditoria.

Regras:

- A exclusao nao deve ser sempre fisica.
- Sem expurgo automatico no MVP.
- Solicitacoes devem ser tratadas caso a caso.
- Automacoes irreversiveis dependem de validacao juridica.

## Segunda Fase

Os casos abaixo ficam fora do MVP:

- Distribuir leads automaticamente.
- Configurar SLA.
- Criar, concluir e cancelar follow-ups como regra operacional obrigatoria.
- Enviar notificacoes.
- Visualizar KPIs e relatorios gerenciais completos.
- Configurar etapas do funil por empresa ou loja.
- Executar parsers dedicados por marketplace.
