# Regras De Negocio

Este documento e a fonte oficial para regras de negocio definidas. Quando uma regra nao estiver definida, a duvida deve ser registrada em [Pendencias de produto](pendencias.md).

## Escopo Do MVP

O MVP confirmado inclui:

    - Tenancy basico: empresas, lojas e usuarios.
- Papeis e permissoes iniciais.
- Captacao de leads por WhatsApp e por e-mail.
- Gestao de leads com origem, duplicidade, historico e pipeline comercial.
- Conversas de WhatsApp por loja, com dono responsavel e vinculo com lead.
- Templates de mensagem da empresa e da loja.
- Fluxo operacional entre pre-venda, vendedor, F&I e avaliador.
- Pipeline com status atuais e etapas opcionais visiveis.
- LGPD basica com registro e tratamento manual de solicitacoes por `ADMIN`.

Ficam para segunda fase:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- KPIs, dashboard gerencial completo e relatorios gerenciais.
- Parsers dedicados para plataformas especificas.
- Configuracao de etapas do funil por empresa ou loja.
- Tela de auditoria.
- Papel/escopo operacional de `AUDITOR`.
- Eventos que criam notificacoes, auditoria ou relatorios.
- Automacoes irreversiveis de LGPD.
- Politica formal de prazos de retencao por tipo de dado.
- Templates padrao definitivos do MVP, quando ainda nao definidos.

## Tenancy, Empresas E Lojas

- Plataforma suporta multiplas empresas.
- Empresa e o agrupador das lojas.
- Loja e a unidade operacional.
- Uma empresa pode ter varias lojas.
- Lojas pertencem a uma empresa.
- Usuarios pertencem a uma empresa.
- Dados como CNPJ, endereco, telefone e razao social pertencem a loja, nao a empresa agrupadora.
- Empresa nao deve concentrar dados fiscais ou operacionais da loja.
- Empresa nao pode ser desativada enquanto houver usuarios ativos vinculados.
- Quando empresa ou loja deixa de operar, usuarios perdem acesso, mas dados e historico permanecem.
- Desativacao nao deve apagar dados historicos.

## Papeis E Permissoes

Papeis do produto:

- `ADMIN`
- `MANAGER`
- `STORE_MANAGER`
- `SELLER`
- `PRE_SALES`
- `F_AND_I`
- `AVALIADOR`

Regras:

- `ADMIN` e global.
- `MANAGER` opera no nivel da empresa.
- `STORE_MANAGER`, `SELLER`, `PRE_SALES`, `F_AND_I` e `AVALIADOR` operam no escopo da loja, salvo regra especifica posterior.
- Cada usuario tem apenas um papel.
- `AUDITOR` fica fora do MVP.
- O papel antigo de atendimento foi removido.
- Usuarios podem editar o proprio perfil.
- O proprio usuario pode redefinir sua senha.
- Deve existir uma visao/acao administrativa para resetar senha de usuario.
- Somente `ADMIN` e gerente podem criar, editar, desativar ou remover usuarios.
- Quando um vendedor for desativado, leads ativos em atendimento devem gerar aviso para o gerente redistribuir para outro vendedor.
- Leads vendidos ou perdidos devem manter historico do vendedor original.

## Autenticacao E Sessao

- Usuarios inativos devem ver mensagem generica ao tentar autenticar.
- Login em multiplas sessoes nao deve ser permitido.
- Deve existir no maximo uma sessao ativa por usuario.
- Rotacao de refresh token revoga imediatamente o token anterior.
- Duracao da sessao: 30 dias.
- Logout revoga todas as sessoes do usuario.
- Desativacao de usuario revoga sessoes ativas.

## Dominio Comercial

- Cliente e um lead; nao deve existir Cliente independente de Lead neste momento.
- Lead representa o outro lado da conversa.
- Conversas pertencem a uma loja.
- Cada conversa deve ter um dono responsavel.
- F&I pode participar de simulacao/proposta, mas nao substitui o dono comercial principal.
- Importacao por WhatsApp e e-mail faz parte da captacao do MVP.

### Item E Veiculo

- Veiculo deve ser entidade estruturada.
- A criacao nao deve ser barrada caso o veiculo estruturado nao seja encontrado.
- Veiculo deve ser tabela filha de Item.
- Item pertence ao usuario.

### Dados Internacionais

- Validacoes por pais devem considerar uso internacional.
- Telefone deve seguir E.164.
- Valor de venda suporta moedas alem de BRL.
- BRL deve ser a moeda default.

## Ciclo De Vida Do Lead

Status conhecidos do funil:

- `NEW`
- `AVAILABLE`
- `ASSIGNED`
- `FIRST_CONTACT`
- `IN_NEGOTIATION`
- `VISIT_SCHEDULED`
- `SIMULATING`
- `PROPOSAL_APPROVED`
- `PROPOSAL_SENT`
- `SOLD`
- `LOST`
- `DUPLICATED`

Regras:

- MVP usa os status atuais.
- `VISIT_SCHEDULED`, `SIMULATING` e `PROPOSAL_APPROVED` sao etapas opcionais.
- `SIMULATING` representa simulacao de banco por F&I.
- `PROPOSAL_APPROVED` representa proposta mais firme, pronta para apresentar ao cliente.
- Etapas opcionais nao impoem ordem obrigatoria.
- Etapas configuraveis ficam para fase futura.
- Lead perdido pode realizar recontato e voltar a ficar ativo.
- Lead vendido tambem pode voltar a ficar ativo em novo contato.
- Quando o lead clica novamente em um anuncio, deve entrar como novo lead, mantendo rastreio/historico relacionado ao contato anterior.
- Campos obrigatorios por origem/anuncio incluem dados do anuncio e dados de contato do lead.
- Dados obrigatorios do anuncio: nome do carro, ano, modelo e valor.
- Dados obrigatorios do lead: nome e telefone WhatsApp.
- Depois do primeiro contato, pode ser alterado o carro/anuncio que o lead clicou e o nome do lead.
- Um lead pode ter outros numeros vinculados.
- Vendedores podem editar valor de venda ou motivo de perda somente com permissao especifica.

## Visibilidade, Busca E Ordenacao De Leads

- Ordenacao padrao da listagem de leads: por chegada.
- Filtros obrigatorios nas telas do MVP nao sao relevantes neste momento.
- Busca textual deve ser normalizada.
- Vendedores nao podem ver leads de outros vendedores.
- `SELLER` pode visualizar leads disponiveis para assumir.
- `SELLER` acessa conversas/leads sob sua responsabilidade.
- `MANAGER` visualiza escopo da empresa.
- `STORE_MANAGER` visualiza escopo da loja.

## Notas, Observacoes, Tags E Historico

- Leads podem ter notas.
- Notas de lead devem ser editaveis.
- Observacoes podem ser editadas.
- Deve existir historico de observacoes.
- Leads podem ter tags.
- Tags nao sao texto livre.
- Tags devem ser cadastraveis, fixas e globais.
- Nomes duplicados de tag nao devem ser permitidos no mesmo lead.
- Deve existir apenas uma tag por tipo no mesmo lead.
- Historico deve armazenar status anterior, novo status, descricao, usuario e data quando aplicavel.
- Eventos automaticos podem ser tratados como detalhe tecnico de auditoria, sem bloquear o MVP.

## Atribuicao E Pipeline

- Atribuicao principal do MVP e manual.
- `PRE_SALES` gera lead; lead fica disponivel no pipeline.
- `SELLER` pode assumir lead disponivel.
- `MANAGER` e `STORE_MANAGER` podem atribuir leads conforme escopo.
- Distribuicao automatica fica para segunda fase.
- Pipeline comercial usa os status atuais.
- Etapas opcionais devem aparecer visualmente no MVP.
- O pipeline deve permitir alteracao de status por arrastar e soltar.
- Nenhuma coluna deve exigir dados adicionais antes de aceitar o lead no MVP.
- Diferenciacao visual entre etapas opcionais e obrigatorias sera decidida na fase de UX.
- Configuracao de funil por empresa/loja fica para fase futura.

## WhatsApp, Conversas E Templates

- Cada loja tem apenas um numero WhatsApp.
- Numero WhatsApp pertence a loja.
- Conversas pertencem a uma loja.
- Cada conversa deve ter dono responsavel.
- Conversa sem vendedor fica na fila da loja.
- Pre-venda assumir fila fica para segunda fase.
- Gerente responde somente se assumir o lead; caso contrario supervisiona.
- F&I pode estar relacionado ao lead nas etapas de simulacao/proposta.
- Eventos de auditoria devem ficar registrados tecnicamente no MVP.
- Tela de auditoria fica para fase posterior.
- Escopo de `AUDITOR` em conversas fica fora do MVP.
- Midias de WhatsApp devem ser armazenadas em S3 ou bucket equivalente.
- Metadados devem referenciar o arquivo armazenado no bucket quando aplicavel.
- Templates podem pertencer a empresa ou a uma loja.
- Templates da empresa podem ser usados por todas as lojas da empresa.
- Templates da loja sao especificos daquela loja.
- Templates podem estar ativos ou inativos.
- Fora da janela de 24 horas do WhatsApp, usuario deve usar template aprovado.
- Gerar link conta como primeiro contato.
- Links gerados devem permanecer imutaveis.
- Templates podem ser criados, editados, desativados ou apagados por gerente geral e `ADMIN`.
- Templates podem ser excluidos depois de usados, mas apenas por exclusao logica.
- Idioma padrao sem `languageCode`: `pt-BR`.
- Nome do template no EAI deve ser exatamente o nome aprovado na Meta.
- Placeholders/componentes da Meta devem ser preenchidos automaticamente com dados disponiveis do lead, loja, vendedor e demais entidades aplicaveis.
- Telefone deve aceitar todos os formatos aceitos pelo WhatsApp/Meta.
- Dados de status de mensagem da Meta devem ser salvos pelo sistema.

## Importacao De Leads Por E-Mail

- Leads podem entrar por WhatsApp e por e-mail.
- E-mails podem vir de plataformas como Webmotors e iCarros, registradas como `LeadSource`.
- Duplicidade usa telefone/WhatsApp e loja.
- Lead repetido deve indicar duplicidade.
- Cada chegada deve ficar registrada no historico.
- Entrada duplicada fica na mesma conversa anterior, mas gera novo lead marcado como duplicado.
- Parsers dedicados ficam para segunda fase.
- Mensagens importadas devem ser marcadas como lidas na conta original.
- Devem ser preservados apenas os dados do lead extraidos do e-mail.
- Contas de e-mail podem ser gerenciadas por `ADMIN` e gerente geral.
- Excluir conta de e-mail deve preservar historico de importacao.
- Testes com falha devem notificar administradores.
- Importacoes com falha devem ser tentadas novamente.

## Dados Padrao, Seeds E Demonstracao

- Dados de seed nao devem existir em deploys de producao.
- Dados de demonstracao devem ser separados de seeds obrigatorios.
- Producao nao deve receber dados de seed/demonstracao automaticamente.
- Templates padrao necessarios para o MVP serao definidos depois.
- Templates padrao podem ser aprovados ou alterados por `ADMIN` e gerente geral.
- Seeds/documentacao de papeis devem incluir `AVALIADOR`.

## LGPD, Auditoria E Retencao

- LGPD basica entra no MVP.
- No MVP, nao implementar automacoes irreversiveis de LGPD.
- No MVP, apenas registrar solicitacoes LGPD e permitir tratamento manual por `ADMIN`.
- Apenas `ADMIN` executa solicitacoes LGPD na fase inicial.
- Deve existir fluxo administrativo para acesso, correcao, bloqueio, anonimizacao ou eliminacao quando aplicavel.
- Exclusao nao deve ser sempre fisica.
- Sistema deve registrar solicitacao, executor, data e acao aplicada.
- Sem prazo automatico de expurgo no MVP.
- Solicitacoes devem ser tratadas caso a caso.
- Validacao juridica fica para fase posterior, antes de qualquer automacao irreversivel.
- Politica formal de prazos de retencao por tipo de dado fica para fase posterior.

## Fora Do MVP

- SLA fica para segunda fase.
- Follow-ups e notificacoes ficam para segunda fase.
- KPIs e relatorios gerenciais ficam para segunda fase.
- Dashboard gerencial completo nao deve bloquear a entrega.
- MVP pode ter visoes operacionais simples.
- Campos/telas relacionados a SLA e follow-ups podem existir como apoio, sem regra operacional obrigatoria.
