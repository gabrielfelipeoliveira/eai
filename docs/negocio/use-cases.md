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

Regras:

- Usuario inativo recebe mensagem generica.
- Login em multiplas sessoes nao e permitido.
- Deve existir no maximo uma sessao ativa por usuario.

### Renovar Sessao

Ator:

- Usuario autenticado com refresh token.

Saidas:

- Novo token de acesso.
- Novo refresh token.

Regras:

- Rotacao de refresh token revoga imediatamente o token anterior.
- Sessao dura 30 dias.

### Logout

Ator:

- Usuario autenticado.

Saidas:

- Todas as sessoes do usuario revogadas.

## Tenancy E Usuarios

### Gerenciar Empresas

Ator:

- `ADMIN`.

Regras:

- Empresa e agrupador de lojas.
- Empresa nao concentra dados fiscais/operacionais da loja.
- Empresa nao pode ser desativada enquanto houver usuarios ativos vinculados.

### Gerenciar Lojas

Ator:

- `ADMIN`.
- `MANAGER`, dentro da empresa.

Entradas:

- Dados da loja, incluindo dados fiscais/operacionais quando aplicavel.

Regras:

- CNPJ, endereco, telefone e razao social pertencem a loja.
- Desativacao de loja preserva dados e historico.

### Gerenciar Usuarios

Ator:

- `ADMIN`.
- Gerente.

Entradas:

- Dados do usuario.
- Papel unico.
- Empresa.
- Loja quando aplicavel.

Regras:

- Papeis conhecidos: `ADMIN`, `MANAGER`, `STORE_MANAGER`, `SELLER`, `PRE_SALES`, `F_AND_I` e `AVALIADOR`.
- Usuario operacional possui loja quando necessario.
- Desativacao de usuario revoga sessoes ativas.
- Desativacao de vendedor com leads ativos em atendimento deve gerar aviso para redistribuicao gerencial.
- Leads vendidos ou perdidos preservam historico do vendedor original.

### Editar Proprio Perfil

Ator:

- Usuario autenticado.

Saidas:

- Perfil atualizado dentro dos limites de permissao.

### Redefinir Senha

Ator:

- Proprio usuario.
- `ADMIN` ou gerente em acao administrativa.

Saidas:

- Senha redefinida conforme politica de seguranca.

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
- Nome do lead.
- Telefone WhatsApp.
- Dados do anuncio: nome do carro, ano, modelo e valor.
- Origem.
- Usuario responsavel opcional.

Saidas:

- Lead criado.
- Historico registrado.

Regras:

- Cliente e um lead.
- Leads podem entrar por WhatsApp, e-mail ou criacao manual.
- Duplicidade e avaliada por telefone/WhatsApp e loja.
- Novo clique em anuncio cria novo lead, mantendo rastreio/historico relacionado ao contato anterior.
- Veiculo estruturado nao pode bloquear criacao quando nao for encontrado.

### Listar E Pesquisar Leads

Ator:

- `ADMIN`.
- `MANAGER`.
- `STORE_MANAGER`.
- `SELLER`.
- `PRE_SALES`.
- `F_AND_I`, quando participar das etapas de simulacao ou proposta.
- `AVALIADOR`, conforme permissao especifica futura.

Entradas:

- Parametros de paginacao.
- Filtros opcionais.
- Busca textual.

Saidas:

- Lista paginada de leads.

Regras:

- Ordenacao padrao: por chegada.
- Busca textual deve ser normalizada.
- `ADMIN` visualiza globalmente.
- `MANAGER` visualiza a empresa.
- `STORE_MANAGER` visualiza a loja.
- `SELLER` visualiza leads disponiveis e leads sob sua responsabilidade.
- Vendedores nao podem ver leads de outros vendedores.

### Assumir Lead Disponivel

Ator:

- `SELLER`.

Saidas:

- Lead atribuido ao vendedor.
- Historico registrado.

Pre-condicoes:

- Lead esta disponivel.
- Vendedor pertence a loja do lead.

### Atribuir Lead Manualmente

Ator:

- `MANAGER`.
- `STORE_MANAGER`.

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

- Pipeline pode alterar status por arrastar e soltar.
- Nenhuma coluna exige dados adicionais antes de aceitar o lead no MVP.
- `VISIT_SCHEDULED`, `SIMULATING` e `PROPOSAL_APPROVED` sao etapas opcionais.
- `F_AND_I` participa das etapas de simulacao e proposta.
- Lead vendido ou perdido pode voltar a ficar ativo por recontato.

### Editar Dados Comerciais Do Lead

Ator:

- Usuario com acesso ao lead.

Regras:

- Depois do primeiro contato, pode ser alterado o carro/anuncio e o nome do lead.
- Lead pode ter outros numeros vinculados.
- Valor de venda e motivo de perda exigem permissao especifica para vendedor.

### Registrar Notas, Observacoes E Tags

Ator:

- Usuario com acesso ao lead.

Regras:

- Notas e observacoes sao editaveis.
- Observacoes criam historico.
- Tags sao globais, cadastraveis e fixas.
- Um lead nao pode ter tag duplicada do mesmo tipo.

## Pipeline

### Visualizar Pipeline

Ator:

- Usuario com acesso ao escopo.

Saidas:

- Leads agrupados por status.

Regras:

- Etapas opcionais aparecem visualmente no MVP.
- Diferenciacao visual entre etapas opcionais e obrigatorias sera decidida na fase de UX.
- Arquitetura deve permitir etapas configuraveis em fase futura.

## WhatsApp E Conversas

### Receber Mensagem Pelo WhatsApp

Ator:

- WhatsApp Cloud API.

Saidas:

- Contato criado ou atualizado.
- Conversa criada ou atualizada.
- Mensagem registrada.
- Midia armazenada em S3/bucket quando aplicavel.

Regras:

- Cada loja deve ter apenas um numero de WhatsApp.
- Conversa pertence a loja do numero.
- Quando nao houver vendedor responsavel, conversa fica na fila da loja.
- Dados de status da Meta devem ser salvos.

### Listar Conversas

Ator:

- `ADMIN`.
- `MANAGER`.
- `STORE_MANAGER`.
- `SELLER`.

Regras:

- `ADMIN` visualiza globalmente.
- `MANAGER` visualiza conversas da empresa.
- `STORE_MANAGER` visualiza conversas da loja.
- `SELLER` visualiza conversas dos leads sob sua responsabilidade.
- Eventos de auditoria ficam registrados tecnicamente no MVP.

### Assumir Conversa Ou Lead Da Fila

Ator:

- `SELLER`.
- `MANAGER`, quando assumir o lead.
- `STORE_MANAGER`, dentro da loja.

Regras:

- Pre-venda assumir conversas da fila fica para segunda fase.
- Gerente pode responder uma conversa somente se assumir o lead.
- Enquanto o lead estiver no nome de um vendedor, gerente apenas supervisiona.

### Enviar Template

Ator:

- Usuario com acesso ao lead ou conversa.

Regras:

- Fora da janela de 24 horas, usuario deve usar template aprovado.
- Nome do template no EAI deve ser exatamente o aprovado na Meta.
- Placeholders/componentes da Meta devem ser preenchidos automaticamente.
- Idioma padrao sem `languageCode`: `pt-BR`.

### Gerenciar Templates

Ator:

- `ADMIN`.
- Gerente geral.

Regras:

- Templates podem ser da empresa ou da loja.
- Templates usados podem ser excluidos apenas por exclusao logica.
- Gerar link conta como primeiro contato.
- Links gerados sao imutaveis.

## Importacao De Leads Por E-Mail

### Sincronizar Leads Por E-Mail

Ator:

- `ADMIN`.
- Gerente geral.
- Scheduler, quando configurado.

Saidas:

- Leads criados.
- Duplicidades marcadas quando aplicavel.
- Historico registrado.
- Mensagens importadas marcadas como lidas.

Regras:

- Plataformas como Webmotors e iCarros devem ser registradas como `LeadSource`.
- Duplicidade usa telefone/WhatsApp e loja.
- Devem ser preservados apenas dados do lead extraidos do e-mail.
- Testes com falha notificam administradores.
- Importacoes com falha devem ser tentadas novamente.
- Excluir conta preserva historico de importacao.

## Dados Padrao

### Gerenciar Seeds E Dados De Demonstracao

Regras:

- Dados de seed nao devem existir em deploys de producao.
- Dados de demonstracao devem ser separados de seeds obrigatorios.
- Seeds/documentacao de papeis devem incluir `AVALIADOR`.
- Templates padrao do MVP serao definidos depois.
- Templates padrao podem ser aprovados ou alterados por `ADMIN` e gerente geral.

## LGPD

### Executar Solicitacao LGPD

Ator:

- `ADMIN`.

Saidas:

- Solicitacao registrada.
- Acao manual registrada.

Regras:

- No MVP, nao implementar automacoes irreversiveis de LGPD.
- A exclusao nao deve ser sempre fisica.
- Sem expurgo automatico no MVP.
- Solicitacoes devem ser tratadas caso a caso.
- Automacoes irreversiveis dependem de validacao juridica em fase posterior.

## Segunda Fase

Os casos abaixo ficam fora do MVP:

- Distribuir leads automaticamente.
- Configurar SLA.
- Criar, concluir e cancelar follow-ups como regra operacional obrigatoria.
- Enviar notificacoes.
- Visualizar KPIs e relatorios gerenciais completos.
- Configurar etapas do funil por empresa ou loja.
- Executar parsers dedicados por marketplace.
- Usar tela de auditoria.
- Definir escopo operacional de `AUDITOR`.
