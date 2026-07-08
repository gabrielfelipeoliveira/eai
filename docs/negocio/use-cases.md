# Casos De Uso

Este documento lista os casos de uso conhecidos sem inventar comportamentos ausentes.

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Usuarios inativos devem ver uma mensagem especifica ou uma mensagem generica de credenciais invalidas?
- Login em multiplas sessoes deve ser permitido?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- A rotacao de refresh token deve revogar imediatamente o refresh token anterior?
- Qual deve ser a duracao da sessao por perfil?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- O logout deve revogar todas as sessoes ou apenas a sessao do dispositivo atual?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais campos de empresa sao obrigatorios em producao?
- O documento da empresa deve ser unico globalmente?
- Empresas podem ser desativadas com lojas ou usuarios ativos?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Um gerente pode criar lojas?
- Lojas podem ser desativadas enquanto existem leads ativos?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Gerentes podem criar vendedores da propria loja?
- Multiplos perfis por usuario devem ser permitidos em producao?
- Empresa e loja sao obrigatorias para todos os perfis?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Usuarios podem editar o proprio perfil?
- Quem pode redefinir senhas?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- O que acontece com leads atribuidos quando um vendedor e desativado?
- A desativacao deve revogar sessoes ativas?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais campos de lead sao obrigatorios por origem?
- Um vendedor pode criar um lead ja atribuido a outro vendedor?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Vendedores devem ver apenas seus leads ou todos os leads da loja?
- Qual ordenacao padrao e esperada pelos usuarios de negocio?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais campos podem ser editados depois do primeiro contato?
- Vendedores podem editar valor de venda ou motivo de perda?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais transicoes de status sao permitidas?
- Quais status exigem campos adicionais?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Um vendedor pode autoatribuir um lead que ja esta atribuido a outra pessoa?
- Gerentes podem reatribuir leads depois que a negociacao comecou?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Observacoes podem ser editadas ou excluidas?
- Observacoes devem criar entradas no historico?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Tags sao texto livre?
- Nomes duplicados de tag devem ser permitidos no mesmo lead?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais status devem aparecer como colunas do pipeline?
- O pipeline deve permitir alteracao de status por arrastar e soltar?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quem fica responsavel pelo follow-up criado?
- Gerentes podem criar follow-ups atribuidos a vendedores?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Um gerente pode concluir follow-up de um vendedor?
- Tarefas concluidas podem ser reabertas?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Gerentes podem configurar distribuicao e SLA?
- Padroes de SLA devem ser globais, por empresa ou por loja?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quem pode executar distribuicao de pendentes?
- A distribuicao automatica deve ocorrer na criacao do lead?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Templates podem ser excluidos depois de usados?
- Templates sao apenas por loja ou podem ser compartilhados no nivel da empresa?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Gerar um link deve contar como primeiro contato?
- Links gerados devem expirar ou permanecer imutaveis?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quem pode gerenciar contas de e-mail?
- Excluir uma conta deve preservar o historico de importacao?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Testes com falha devem notificar administradores?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Mensagens importadas devem ser marcadas como lidas?
- Importacoes com falha devem ser tentadas novamente?

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

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais KPIs sao oficiais para o MVP?
- Quais atores podem visualizar valores financeiros?
