# Pendencias De Produto

Este documento centraliza as duvidas de negocio que precisam de decisao do Product Owner.

Os demais documentos de negocio devem conter preferencialmente informacoes ja definidas. Quando uma regra, fluxo, ator ou prioridade ainda nao estiver clara, registre a duvida aqui em vez de espalhar perguntas pelos documentos de referencia.

## Como Usar

- Antes de implementar funcionalidade de negocio, revise este documento.
- Quando uma pergunta for respondida, atualize a documentacao de negocio afetada.
- Depois que a regra estiver documentada no arquivo oficial, remova ou marque a pendencia como resolvida neste documento.
- Nao implemente comportamento baseado em suposicao.

## Tenancy E Escopo

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Um gerente pode operar em todas as lojas da empresa quando nao estiver vinculado a uma loja especifica?
- Todo usuario nao-admin deve sempre estar vinculado a uma empresa e a uma loja?
- Recepcionistas e auditores devem ter escopo por empresa ou por loja?
- Como resolver o conflito atual entre servicos que exigem `storeId` para usuarios nao-admin e servicos que permitem gerente escopado apenas por empresa?

## Papeis E Permissoes

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- O que `RECEPTIONIST` pode fazer?
- O que `AUDITOR` pode fazer?
- `MANAGER` pode criar usuarios ou apenas visualiza-los?
- Gerentes podem criar vendedores da propria loja?
- Multiplos perfis por usuario devem ser permitidos em producao?
- Empresa e loja sao obrigatorias para todos os perfis?
- Usuarios podem editar o proprio perfil?
- Quem pode redefinir senhas?
- O que acontece com leads atribuidos quando um vendedor e desativado?
- A desativacao de usuario deve revogar sessoes ativas?

## Autenticacao E Sessao

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Usuarios inativos devem ver mensagem especifica ou mensagem generica de credenciais invalidas?
- Login em multiplas sessoes deve ser permitido?
- A rotacao de refresh token deve revogar imediatamente o refresh token anterior?
- Qual deve ser a duracao da sessao por perfil?
- O logout deve revogar todas as sessoes ou apenas a sessao do dispositivo atual?

## Empresas E Lojas

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais campos de empresa sao obrigatorios em producao?
- O documento da empresa deve ser unico globalmente?
- Empresas podem ser desativadas com lojas ou usuarios ativos?
- Um gerente pode criar lojas?
- Lojas podem ser desativadas enquanto existem leads ativos?

## Ciclo De Vida Do Lead

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais transicoes de status do lead sao permitidas?
- Quais transicoes de status do lead sao proibidas?
- Quais status exigem campos adicionais?
- Um lead vendido ou perdido pode ser reaberto?
- Um lead duplicado pode voltar ao funil?
- Atribuir um lead manual sempre deve move-lo para `ASSIGNED`?
- Autoatribuicao deve ficar restrita a leads `NEW` ou `AVAILABLE`?
- Quais campos de lead sao obrigatorios por origem?
- Um vendedor pode criar um lead ja atribuido a outro vendedor?
- Quais campos podem ser editados depois do primeiro contato?
- Vendedores podem editar valor de venda ou motivo de perda?

## Visibilidade E Pesquisa De Leads

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- `SELLER` pode ver leads nao atribuidos da loja?
- `SELLER` pode ver leads atribuidos a outros vendedores da mesma loja?
- Vendedores devem ver apenas seus leads ou todos os leads da loja?
- Qual ordenacao padrao e esperada pelos usuarios de negocio na listagem de leads?

## Atribuicao E Distribuicao De Leads

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Gerentes podem atribuir leads a si mesmos?
- Um vendedor pode autoatribuir um lead que ja esta atribuido a outra pessoa?
- Gerentes podem reatribuir leads depois que a negociacao comecou?
- Atribuicao deve considerar horario de trabalho ou disponibilidade do vendedor?
- Atribuicao deve considerar origem do lead ou especialidade do vendedor?
- O que deve acontecer quando nao houver vendedor elegivel?
- Um lead pode ser reatribuido livremente depois do primeiro contato?
- Quem pode executar distribuicao de pendentes?
- A distribuicao automatica deve ocorrer na criacao do lead?
- Gerentes podem configurar distribuicao e SLA?
- Padroes de SLA devem ser globais, por empresa ou por loja?

## SLA

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- O SLA conta tempo corrido ou horario comercial?
- O SLA pausa em algum status do lead?
- Quais usuarios podem editar a politica de SLA?
- Violacoes de SLA devem criar notificacoes ou tarefas?
- Qual e o fluxo de escalonamento para leads atrasados?

## Follow-Ups

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quem fica responsavel pelo follow-up criado?
- Gerentes podem criar follow-ups atribuidos a vendedores?
- Vendedores podem concluir follow-ups criados por gerentes?
- Um gerente pode concluir follow-up de um vendedor?
- Follow-ups devem gerar lembretes ou notificacoes?
- Follow-ups podem ser reagendados?
- Tarefas concluidas podem ser reabertas?

## Notas, Tags E Historico

Status:
PENDENTE DE DEFINIÇÃO

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
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- A Sprint 1 aprovou a base tecnica do WhatsApp Cloud API, mas a visao/roadmap ainda registram integracao nativa com WhatsApp Business API como fora de escopo ou ideia futura. Essa documentacao deve ser atualizada para refletir o novo escopo aprovado?
- Eventos recebidos pelo webhook do WhatsApp devem criar leads, atualizar comunicacoes existentes, alimentar uma caixa de entrada ou apenas ficar registrados ate nova decisao?
- Qual tenant, empresa ou loja deve ser associado a cada numero do WhatsApp Cloud API?
- Gerar um link deve contar como primeiro contato?
- Links gerados devem expirar ou permanecer imutaveis?
- O historico de comunicacao deve guardar apenas links gerados ou conversas reais futuramente?
- Templates podem ser da empresa inteira ou somente da loja?
- Templates sao apenas por loja ou podem ser compartilhados no nivel da empresa?
- Quem pode criar, editar, desativar ou apagar templates?
- Templates podem ser excluidos depois de usados?

## Importador De Leads Por E-Mail

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais origens de lead e marketplaces precisam de parsers dedicados primeiro?
- Mensagens importadas devem ser marcadas como lidas?
- Leads duplicados devem ser ignorados, mesclados ou sempre criados como `DUPLICATED`?
- A janela de duplicidade de 7 dias esta correta para todas as lojas?
- Quais informacoes precisam ser preservadas do e-mail original?
- Quem pode gerenciar contas de e-mail?
- Excluir uma conta deve preservar o historico de importacao?
- Testes com falha devem notificar administradores?
- Importacoes com falha devem ser tentadas novamente?

## Dashboard E Relatorios

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais sao os KPIs oficiais do MVP?
- Quais relatorios sao obrigatorios para gerentes?
- Relatorios devem usar dados operacionais em tempo real ou snapshots agregados?
- Quais datas guiam metricas de conversao e tempo de resposta?
- Quais atores podem visualizar valores financeiros?

## Dominio Conceitual

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Importacao por e-mail deve ser considerada parte da captacao de leads ou um contexto separado de integracao?
- Atendimento ao cliente deve ser um contexto separado da gestao do funil comercial?
- Notas, tags, comunicacoes e follow-ups fazem parte das invariantes do agregado Lead ou sao registros operacionais independentes?
- Deve existir uma entidade Cliente independente de Lead?
- Veiculo deve ser modelado futuramente como entidade estruturada?
- Quais campos exigem validacao especifica por pais?
- Normalizacao de telefone deve seguir apenas numeros brasileiros?
- Valor de venda deve suportar moedas alem de BRL?
- Quais eventos de dominio devem criar notificacoes?
- Quais eventos devem ser auditaveis por compliance?
- Quais eventos devem alimentar relatorios?

## Pipeline

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais status devem aparecer como colunas do pipeline?
- O pipeline deve permitir alteracao de status por arrastar e soltar?

## Escopo E Roadmap

Status:
PENDENTE DE DEFINIÇÃO

Perguntas para o Product Owner:

- Quais itens fora de escopo devem ser considerados escopo futuro explicito e quais devem ficar fora do produto?
- Qual e o limite esperado do MVP para uma liberacao ao cliente?
- Quais papeis de usuario sao obrigatorios no MVP alem dos papeis ja modelados no sistema?
- Quais modulos candidatos sao obrigatorios no MVP?
- Qual e a menor entrega que gera valor para uma concessionaria?
- Quais papeis de usuario devem existir no primeiro dia?
- Quais relatorios operacionais sao obrigatorios no MVP?
- Quais workflows precisam estar completos antes de um piloto pago?
- Quais integracoes sao obrigatorias para as lojas na primeira versao?
- Quais workflows avancados tem prioridade comercial?
- Quais sistemas externos precisam ser integrados?
- Quais ideias futuras sao apostas estrategicas?
- Quais ideias devem ser explicitamente excluidas?
