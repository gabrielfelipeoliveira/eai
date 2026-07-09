# Casos De Uso

Este documento lista os casos de uso conhecidos sem inventar comportamentos ausentes. Duvidas sobre entradas, atores, regras e excecoes ficam centralizadas em [Pendencias de produto](pendencias.md).

## Autenticacao

### Login

Ator:

- Usuario cadastrado

Entradas:

- E-mail
- Senha

Saidas:

- Token de acesso
- Refresh token
- Tipo do token

Pre-condicoes:

- Usuario existe.
- Usuario esta ativo.
- Senha corresponde ao hash armazenado.

Pos-condicoes:

- Refresh token e persistido.

### Renovar Sessao

Ator:

- Cliente autenticado com refresh token

Entradas:

- Refresh token

Saidas:

- Novo token de acesso
- Novo refresh token

Pre-condicoes:

- Refresh token existe e e valido.

Pos-condicoes:

- Continuidade da sessao e mantida.

### Logout

Ator:

- Usuario autenticado

Entradas:

- Sessao atual do usuario

Saidas:

- Resposta vazia

Pre-condicoes:

- Usuario esta autenticado.

Pos-condicoes:

- Refresh tokens ativos do usuario sao revogados conforme a implementacao atual.

## Gestao De Tenants

### Gerenciar Empresas

Ator:

- Admin

Entradas:

- Dados da empresa

Saidas:

- Registros de empresa

Pre-condicoes:

- Ator possui permissao.

Pos-condicoes:

- Empresa e criada ou atualizada.

### Gerenciar Lojas

Ator:

- Admin
- Gerente conforme documentacao e codigo atuais, sujeito ao escopo do tenant

Entradas:

- Dados da loja

Saidas:

- Registros de loja

Pre-condicoes:

- Empresa existe.
- Ator possui permissao para a empresa.

Pos-condicoes:

- Loja e criada ou atualizada.

## Gestao De Usuarios

### Criar Usuario

Ator:

- Admin conforme regras atuais da API

Entradas:

- Nome
- E-mail
- Senha
- Telefone opcional
- Cargo opcional
- Identidade da empresa
- Identidade da loja
- Perfis

Saidas:

- Registro do usuario sem senha

Pre-condicoes:

- E-mail e unico.
- Referencias de tenant sao validas quando informadas.

Pos-condicoes:

- Usuario e criado ativo conforme comportamento atual do dominio.

### Atualizar Usuario

Ator:

- Admin

Entradas:

- Campos editaveis do usuario

Saidas:

- Registro de usuario atualizado

Pre-condicoes:

- Usuario existe.

Pos-condicoes:

- Dados do usuario e, opcionalmente, senha sao atualizados.

### Ativar Ou Desativar Usuario

Ator:

- Admin

Entradas:

- Identidade do usuario

Saidas:

- Registro de usuario atualizado

Pre-condicoes:

- Usuario existe.

Pos-condicoes:

- Status do usuario e alterado.

## Gestao De Leads

### Criar Lead

Ator:

- Admin
- Gerente
- Vendedor

Entradas:

- Identidade da empresa
- Identidade da loja
- Dados do cliente
- Veiculo de interesse
- Origem
- Mensagem original
- Usuario responsavel opcional
- Motivo de perda opcional
- Valor de venda opcional

Saidas:

- Lead criado

Pre-condicoes:

- Referencias de tenant sao validas.
- Ator pode usar o tenant.
- Usuario atribuido pertence a loja do lead quando informado.

Pos-condicoes:

- Lead e criado.
- Historico e registrado.

### Listar E Pesquisar Leads

Ator:

- Admin
- Gerente
- Vendedor

Entradas:

- Parametros de paginacao
- Filtros opcionais

Saidas:

- Lista paginada de leads

Pre-condicoes:

- Ator esta autenticado.

Pos-condicoes:

- Nenhum dado e alterado.

### Atualizar Lead

Ator:

- Admin
- Gerente
- Vendedor conforme acesso atual do endpoint e validacoes do servico

Entradas:

- Identidade do lead
- Campos editaveis do lead

Saidas:

- Lead atualizado

Pre-condicoes:

- Lead existe.
- Ator pode acessar o lead.
- Referencias de tenant sao validas.

Pos-condicoes:

- Lead e atualizado.
- Historico e registrado se o status mudar.

### Alterar Status Do Lead

Ator:

- Admin
- Gerente
- Vendedor

Entradas:

- Identidade do lead
- Novo status
- Descricao opcional

Saidas:

- Lead atualizado

Pre-condicoes:

- Lead existe.
- Ator pode acessar o lead.

Pos-condicoes:

- Status do lead e alterado.
- Historico e registrado.

### Atribuir Lead

Ator:

- Admin
- Gerente
- Vendedor para autoatribuicao conforme comportamento atual

Entradas:

- Identidade do lead
- Identidade do usuario

Saidas:

- Lead atribuido

Pre-condicoes:

- Lead existe.
- Usuario atribuido pertence a loja do lead.
- Ator possui permissao.

Pos-condicoes:

- Lead passa a ter usuario responsavel.
- Status do lead passa para atribuido.
- Historico e registrado.

### Adicionar Observacao

Ator:

- Usuario com acesso ao lead

Entradas:

- Identidade do lead
- Texto da observacao

Saidas:

- Observacao criada

Pre-condicoes:

- Lead existe.
- Ator pode acessar o lead.

Pos-condicoes:

- Observacao e armazenada.

### Gerenciar Tags

Ator:

- Usuario com acesso ao lead

Entradas:

- Identidade do lead
- Nome da tag

Saidas:

- Lista de tags ou tag criada

Pre-condicoes:

- Lead existe.
- Ator pode acessar o lead.

Pos-condicoes:

- Tag e adicionada ou removida.

## Pipeline

### Visualizar Pipeline

Ator:

- Admin
- Gerente
- Vendedor

Entradas:

- Contexto do usuario autenticado

Saidas:

- Leads agrupados por status

Pre-condicoes:

- Ator esta autenticado.

Pos-condicoes:

- Nenhum dado e alterado.

## Follow-Ups

### Criar Follow-Up

Ator:

- Usuario com acesso ao lead

Entradas:

- Identidade do lead
- Titulo
- Descricao opcional
- Data de vencimento

Saidas:

- Follow-up criado

Pre-condicoes:

- Lead existe.
- Ator pode acessar o lead.

Pos-condicoes:

- Follow-up e criado.
- Historico do lead e registrado.

### Concluir Ou Cancelar Follow-Up

Ator:

- Usuario com acesso conforme regras atuais do servico

Entradas:

- Identidade do follow-up

Saidas:

- Follow-up atualizado

Pre-condicoes:

- Follow-up existe.
- Ator pode acessar o lead ou tarefa relacionada.

Pos-condicoes:

- Status do follow-up e alterado.
- Historico do lead e registrado.

## Distribuicao E SLA

### Configurar Distribuicao E SLA

Ator:

- Admin
- Gerente conforme acesso atual do endpoint

Entradas:

- Identidade da empresa
- Identidade da loja
- Modo de distribuicao
- Flag ativo
- Minutos de SLA
- Flag de SLA ativo

Saidas:

- Configuracoes de distribuicao e SLA atualizadas

Pre-condicoes:

- Loja pertence a empresa.
- Ator possui permissao.

Pos-condicoes:

- Configuracoes de distribuicao da loja sao atualizadas.

### Atribuir Leads Automaticamente

Ator:

- Admin
- Gerente
- Vendedor para atribuicao automatica de um lead conforme regras atuais de acesso

Entradas:

- Identidade do lead ou fila de leads pendentes

Saidas:

- Leads atribuidos

Pre-condicoes:

- Existem vendedores elegiveis.
- Configuracao de distribuicao existe ou padroes sao aplicados.

Pos-condicoes:

- Leads sao atribuidos conforme estrategia configurada.
- Historico e registrado.

## Templates De Comunicacao

### Gerenciar Templates

Ator:

- Admin
- Gerente conforme regras atuais de acesso

Entradas:

- Dados do template

Saidas:

- Registro de template

Pre-condicoes:

- Loja pertence a empresa.
- Ator possui permissao.

Pos-condicoes:

- Template e criado, atualizado ou excluido.

### Gerar Link Do WhatsApp

Ator:

- Usuario com acesso ao lead

Entradas:

- Identidade do lead
- Identidade do template

Saidas:

- Mensagem renderizada
- URL do WhatsApp
- Identidade do registro de comunicacao

Pre-condicoes:

- Lead existe.
- Template esta ativo.
- Template pertence a mesma loja do lead.
- Lead possui telefone.

Pos-condicoes:

- Registro de comunicacao e armazenado.
- Mensagem de conversa de saida e registrada no historico basico da conversa.

### Enviar Template Pelo WhatsApp

Ator:

- Usuario com acesso ao lead

Entradas:

- Identidade do lead
- Identidade do template
- Codigo de idioma opcional

Saidas:

- Mensagem renderizada
- Identidade do registro de comunicacao
- Identidade da mensagem de conversa
- Status inicial da mensagem enviada
- Identidade externa da mensagem quando retornada pela WhatsApp Cloud API
- Retorno bruto da WhatsApp Cloud API

Pre-condicoes:

- Lead existe.
- Template esta ativo.
- Template pertence a mesma loja do lead.
- Lead possui telefone valido para envio ao WhatsApp.
- Configuracao de envio da WhatsApp Cloud API esta disponivel.

Pos-condicoes:

- Template e enviado para a WhatsApp Cloud API.
- Registro de comunicacao e armazenado.
- Mensagem de conversa de saida e registrada com status `SENT` quando a API aceita o envio.
- Falha de envio e registrada com status `FAILED` quando a API rejeita ou a chamada falha.
- Retorno da API fica vinculado a mensagem de conversa.

## Conversas De WhatsApp

### Listar Conversas

Ator:

- Admin
- Gerente
- Vendedor

Entradas:

- Contexto do usuario autenticado
- Filtro opcional por vendedor
- Filtro opcional por status da ultima mensagem
- Filtro opcional por periodo da ultima interacao

Saidas:

- Conversas visiveis ao usuario.
- Dados principais do lead ou contato.
- Telefone.
- Ultima mensagem.
- Data e hora da ultima interacao.
- Quantidade de mensagens nao lidas.

Pre-condicoes:

- Ator esta autenticado.
- Ator possui acesso ao tenant da conversa.

Pos-condicoes:

- Nenhum dado e alterado.

Regras conhecidas:

- Vendedores visualizam apenas conversas sob sua responsabilidade.
- Gerentes visualizam conversas dentro do seu escopo de tenant.
- Admins visualizam todas as conversas.
- Conversas sao ordenadas pela ultima interacao registrada.
- Mensagens recebidas com status `RECEIVED` contam como nao lidas na listagem.

### Receber Mensagem Pelo Webhook

Ator:

- WhatsApp Cloud API

Entradas:

- Payload do webhook da Meta.
- Configuracao de empresa e loja do canal WhatsApp.

Saidas:

- Resposta aceita pelo backend.

Pre-condicoes:

- Webhook publico esta configurado.
- Empresa e loja do canal WhatsApp estao configuradas.

Pos-condicoes:

- Contato de WhatsApp e criado ou atualizado pelo telefone.
- Conversa e criada automaticamente para o contato.
- Quando existir lead da loja com telefone correspondente, a conversa fica vinculada ao lead.
- Mensagem recebida e armazenada com direcao de entrada, tipo e status recebido.

### Consultar Historico Basico Da Conversa

Ator:

- Admin
- Gerente
- Vendedor

Entradas:

- Identidade da conversa ou identidade do lead.

Saidas:

- Mensagens vinculadas a conversa.

Pre-condicoes:

- Ator esta autenticado.
- Ator possui acesso ao tenant da conversa.

Pos-condicoes:

- Mensagens recebidas da conversa com status `RECEIVED` sao marcadas como `READ`.
- Acesso de gerente ou admin e registrado para auditoria.

### Enviar Texto Livre Na Conversa

Ator:

- Admin
- Gerente
- Vendedor

Entradas:

- Identidade da conversa.
- Conteudo textual.

Saidas:

- Mensagem enviada registrada na conversa.
- Status inicial da mensagem.
- Identidade externa da mensagem quando retornada pela WhatsApp Cloud API.

Pre-condicoes:

- Ator esta autenticado.
- Ator possui acesso ao tenant da conversa.
- Conversa possui mensagem recebida do cliente nos ultimos 24 horas.
- Configuracao de envio da WhatsApp Cloud API esta disponivel.

Pos-condicoes:

- Texto livre e enviado para WhatsApp Cloud API.
- Mensagem de conversa de saida e registrada com status `SENT` quando API aceita o envio.
- Falha de envio e registrada com status `FAILED` quando API rejeita ou chamada falha.
- Fora da janela de 24 horas, envio e bloqueado e o usuario deve usar template aprovado.

## Importacao De Leads Por E-Mail

### Gerenciar Contas De E-Mail

Ator:

- Admin
- Gerente conforme regras atuais de acesso

Entradas:

- Configuracoes da conta de e-mail

Saidas:

- Registro da conta de e-mail sem senha

Pre-condicoes:

- Loja pertence a empresa.
- Ator possui permissao.

Pos-condicoes:

- Conta de e-mail e criada, atualizada ou excluida.

### Testar Conexao De E-Mail

Ator:

- Usuario com acesso a conta

Entradas:

- Identidade da conta de e-mail

Saidas:

- Resultado de importacao/teste

Pre-condicoes:

- Conta existe.
- Credenciais estao disponiveis.

Pos-condicoes:

- Nenhum lead e criado.

### Sincronizar Leads Por E-Mail

Ator:

- Usuario com acesso a conta ou scheduler

Entradas:

- Identidade da conta de e-mail

Saidas:

- Resultado da importacao

Pre-condicoes:

- Conta existe e esta ativa.
- Conexao IMAP funciona.

Pos-condicoes:

- Leads podem ser criados.
- Data de ultima leitura da conta pode ser atualizada.

## Dashboard

### Visualizar Dashboard

Ator:

- Usuario autenticado com acesso a relatorios

Entradas:

- Filtros opcionais

Saidas:

- Metricas operacionais e comerciais

Pre-condicoes:

- Ator esta autenticado.
- Ator possui escopo de tenant conforme perfil.

Pos-condicoes:

- Nenhum dado e alterado.
