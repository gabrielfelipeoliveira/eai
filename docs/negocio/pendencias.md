# Pendencias De Produto

Este documento centraliza duvidas de negocio que ainda precisam de decisao. Regras ja definidas devem ficar em [Regras de negocio](business-rules.md).

## Como Usar

- Antes de implementar funcionalidade de negocio, revise este documento.
- Quando uma pergunta for respondida, atualize a documentacao de negocio afetada.
- Depois que a regra estiver documentada no arquivo oficial, remova ou marque a pendencia como resolvida neste documento.
- Nao implemente comportamento baseado em suposicao.

## Autenticacao E Sessao

Status:
PENDENTE DE DEFINICAO

Perguntas para o Product Owner:

- Usuarios inativos devem ver mensagem especifica ou mensagem generica de credenciais invalidas?
- Login em multiplas sessoes deve ser permitido?
- A rotacao de refresh token deve revogar imediatamente o refresh token anterior?
- Qual deve ser a duracao da sessao por perfil?
- O logout deve revogar todas as sessoes ou apenas a sessao do dispositivo atual?
- A desativacao de usuario deve revogar sessoes ativas?

## Empresas E Lojas

Status:
PENDENTE DE DEFINICAO

Perguntas para o Product Owner:

- Quais campos de empresa sao obrigatorios em producao?
- O documento da empresa deve ser unico globalmente?
- Empresas podem ser desativadas com lojas ou usuarios ativos?
- Lojas podem ser desativadas enquanto existem leads ativos?

## Usuarios E Permissoes

Status:
PARCIALMENTE DEFINIDO

Regras ja definidas:

- Papeis do MVP: `ADMIN`, `MANAGER`, `STORE_MANAGER`, `SELLER`, `PRE_SALES` e `F_AND_I`.
- `AUDITOR` fica fora do MVP.
- Cada usuario tem apenas um papel.
- `ADMIN` e global.

Perguntas restantes:

- Usuarios podem editar o proprio perfil?
- Quem pode redefinir senhas?
- O que acontece com leads atribuidos quando um vendedor e desativado?
- Quem pode criar, editar, desativar ou remover usuarios dentro de cada papel?

## Ciclo De Vida Do Lead

Status:
PARCIALMENTE DEFINIDO

Regras ja definidas:

- O MVP usa os status atuais e exibe etapas opcionais no pipeline.
- `VISIT_SCHEDULED`, `SIMULATING` e `PROPOSAL_APPROVED` sao opcionais.
- `SIMULATING` e `PROPOSAL_APPROVED` ficam conceitualmente entre negociacao e proposta enviada, sem ordem obrigatoria.
- Etapas configuraveis ficam para fase futura.

Perguntas restantes:

- Um lead vendido ou perdido pode ser reaberto?
- Quais campos de lead sao obrigatorios por origem?
- Quais campos podem ser editados depois do primeiro contato?
- Vendedores podem editar valor de venda ou motivo de perda?

## Notas, Tags E Historico

Status:
PENDENTE DE DEFINICAO

Perguntas para o Product Owner:

- Notas de lead sao editaveis ou imutaveis?
- Observacoes podem ser editadas ou excluidas?
- Observacoes devem criar entradas no historico?
- Tags sao texto livre?
- Tags sao globais, escopadas por loja ou texto livre por lead?
- Nomes duplicados de tag devem ser permitidos no mesmo lead?
- Historico deve suportar atores de sistema explicitamente?

## Templates E WhatsApp

Status:
PARCIALMENTE DEFINIDO

Regras ja definidas:

- Cada loja deve ter apenas um numero de WhatsApp.
- Numero de WhatsApp pertence a loja.
- Templates podem ser globais da empresa ou especificos da loja.
- Conversa sem vendedor fica na fila da loja.
- Pre-venda assumir conversas da fila fica para segunda fase.
- Gerente responde conversa apenas se assumir o lead; caso contrario, supervisiona.

Perguntas restantes:

- Gerar um link deve contar como primeiro contato?
- Links gerados devem expirar ou permanecer imutaveis?
- Quais transicoes oficiais de status de mensagem devem ser aplicadas a partir dos eventos de status da Meta?
- Mensagens de imagem, audio e documento devem armazenar apenas metadados ou tambem baixar e guardar o arquivo?
- Quem pode criar, editar, desativar ou apagar templates?
- Templates podem ser excluidos depois de usados?
- Qual idioma padrao deve ser usado no envio de templates quando o usuario nao informar `languageCode`?
- O nome do template cadastrado no EAI deve sempre ser exatamente o nome aprovado na Meta?
- Como mapear placeholders do EAI para variaveis numericas e componentes aprovados na Meta quando houver header, botoes ou midia?
- A validacao de telefone deve seguir apenas numeros brasileiros ou qualquer E.164 aceito pelo WhatsApp?

## Importador De Leads Por E-Mail

Status:
PARCIALMENTE DEFINIDO

Regras ja definidas:

- Leads podem chegar por WhatsApp ou e-mail.
- E-mails podem vir de plataformas como Webmotors e iCarros, registradas como `LeadSource`.
- Duplicidade usa telefone/WhatsApp e loja.
- Entrada duplicada gera novo lead marcado como duplicado, mantendo relacao com a conversa anterior.

Perguntas restantes:

- Mensagens importadas devem ser marcadas como lidas?
- Quais informacoes precisam ser preservadas do e-mail original?
- Quem pode gerenciar contas de e-mail?
- Excluir uma conta deve preservar o historico de importacao?
- Testes com falha devem notificar administradores?
- Importacoes com falha devem ser tentadas novamente?

## Segunda Fase

Status:
FORA DO MVP

Itens definidos para segunda fase:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- KPIs, dashboards e relatorios gerenciais.
- Parsers dedicados para plataformas especificas.
- Configuracao de etapas do funil por empresa ou loja.
- Regras avancadas de auditoria, retencao e compliance.

Perguntas futuras:

- Quais KPIs oficiais devem existir?
- Quais relatorios sao obrigatorios para gerentes?
- Relatorios devem usar dados operacionais em tempo real ou snapshots agregados?
- Quais datas guiam metricas de conversao e tempo de resposta?
- Quais workflows de notificacao e follow-up sao obrigatorios?
- Como a distribuicao automatica deve priorizar vendedores?
- Como configurar funil por empresa ou loja?

## LGPD, Auditoria E Retencao

Status:
PARCIALMENTE DEFINIDO

Regras ja definidas:

- O MVP deve ter fluxo administrativo LGPD.
- Apenas `ADMIN` executa solicitacoes LGPD na fase inicial.
- Sem expurgo automatico no MVP.
- Solicitacoes devem ser tratadas caso a caso.
- A exclusao pode ser anonimizacao, eliminacao ou bloqueio, conforme base legal e necessidade operacional.

Perguntas restantes:

- Validar a direcao LGPD com assessoria juridica antes de implementar automacoes irreversiveis.
- Definir politica formal de prazos de retencao por tipo de dado em fase posterior.
