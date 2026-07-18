# Modelo De Dominio

Este documento descreve o dominio conceitual definido. Ele evita detalhes tecnicos de framework. Duvidas de modelagem e regras ainda nao decididas ficam em [Pendencias de produto](pendencias.md).

## Contextos De Dominio

- Identidade, autenticacao e acesso.
- Tenancy.
- Gestao de leads.
- Atribuicao manual de leads.
- Pipeline comercial.
- Conversas de WhatsApp.
- Templates de comunicacao.
- Importacao de leads por e-mail.
- Item e veiculo.
- LGPD, auditoria e retencao.

Ficam para segunda fase:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- KPIs, dashboards e relatorios gerenciais.
- Configuracao de etapas do funil por empresa ou loja.
- Tela de auditoria.
- Escopo operacional de `AUDITOR` fica fora do MVP.
- Automacoes irreversiveis de LGPD.

## Entidades Principais

### Empresa

Representa o agrupador das lojas de uma organizacao cliente.

Atributos conhecidos:

- Identidade.
- Nome.
- Status.
- Datas de criacao e atualizacao.

Regras definidas:

- Empresa nao concentra CNPJ, endereco, telefone ou razao social operacional da loja.
- Empresa nao pode ser desativada enquanto houver usuarios ativos vinculados.
- Quando uma empresa deixa de operar, usuarios perdem acesso, mas dados e historico permanecem.

### Loja

Representa uma loja ou unidade operacional vinculada a uma empresa.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Nome.
- CNPJ ou documento fiscal equivalente.
- Razao social.
- Endereco.
- Cidade.
- Estado/regiao.
- Pais.
- Telefone.
- Numero de WhatsApp da loja.
- Status.
- Datas de criacao e atualizacao.

Regras definidas:

- Cada loja deve ter apenas um numero de WhatsApp.
- Dados fiscais e operacionais, como CNPJ, endereco, telefone e razao social, pertencem a loja.
- Quando uma loja deixa de operar, usuarios relacionados perdem acesso, mas dados e historico permanecem.

### Usuario

Representa uma pessoa que autentica e opera no sistema.

Atributos conhecidos:

- Identidade.
- Nome.
- E-mail.
- Hash da senha.
- Telefone.
- Cargo.
- Identidade da empresa.
- Identidade da loja quando aplicavel.
- Status.
- Papel unico.
- Datas de criacao e atualizacao.

Papeis do produto:

- `ADMIN`
- `MANAGER`
- `STORE_MANAGER`
- `SELLER`
- `PRE_SALES`
- `F_AND_I`
- `AVALIADOR`

Regras definidas:

- `ADMIN` e global.
- `MANAGER` opera no nivel da empresa.
- `STORE_MANAGER`, `SELLER`, `PRE_SALES`, `F_AND_I` e `AVALIADOR` operam no escopo da loja, salvo regra especifica posterior.
- Cada usuario tem apenas um papel.
- `AUDITOR` fica fora do MVP.
- Usuarios podem editar o proprio perfil.
- Usuarios podem redefinir a propria senha.
- `ADMIN` e gerente podem resetar senha de usuarios.
- `ADMIN` e gerente podem criar, editar, desativar ou remover usuarios.
- Desativacao de usuario revoga sessoes ativas.

### Sessao

Representa uma sessao autenticada do usuario.

Regras definidas:

- Login em multiplas sessoes nao e permitido.
- Deve existir no maximo uma sessao ativa por usuario.
- Refresh token anterior e revogado imediatamente na rotacao.
- Sessao dura 30 dias.
- Logout revoga todas as sessoes.
- Usuario inativo recebe mensagem generica ao tentar autenticar.

### Lead

Representa uma oportunidade comercial. Cliente e tratado como Lead no MVP.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Nome do lead.
- Telefone WhatsApp principal em E.164.
- Telefones adicionais vinculados.
- E-mail quando disponivel.
- Cidade ou localidade quando disponivel.
- Dados do anuncio: nome do carro, ano, modelo e valor.
- Item relacionado.
- Origem.
- Mensagem original.
- Status.
- Usuario responsavel principal.
- Relacao com F&I quando aplicavel.
- Indicacao de duplicidade.
- Relacao com lead ou conversa anterior quando duplicado.
- Data de atribuicao.
- Data do primeiro contato.
- Data do ultimo contato.
- Motivo de perda.
- Valor de venda e moeda.
- Datas de criacao e atualizacao.

Status conhecidos:

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

Regras definidas:

- Lead representa o cliente.
- `VISIT_SCHEDULED`, `SIMULATING` e `PROPOSAL_APPROVED` sao etapas opcionais.
- As etapas opcionais devem aparecer visualmente no pipeline do MVP.
- Etapas opcionais nao impoem ordem obrigatoria.
- Lead perdido ou vendido pode voltar a ficar ativo por recontato.
- Novo clique em anuncio cria novo lead, mantendo rastreio/historico relacionado ao contato anterior.
- Duplicidade e validada por telefone/WhatsApp e loja.
- Lead duplicado inicia com dados proprios, preserva historico da nova chegada e relacao com lead/conversa anterior.
- Vendedores nao podem ver leads de outros vendedores.
- Vendedores podem editar valor de venda ou motivo de perda somente com permissao especifica.

### Item

Representa o item de interesse do lead.

Atributos conhecidos:

- Identidade.
- Identidade do usuario proprietario.
- Dados basicos do item.
- Datas de criacao e atualizacao.

Regra definida:

- Item pertence ao usuario.

### Veiculo

Representa veiculo estruturado relacionado a um item.

Atributos conhecidos:

- Identidade.
- Identidade do item.
- Nome.
- Ano.
- Modelo.
- Valor.
- Datas de criacao e atualizacao.

Regras definidas:

- Veiculo deve ser entidade estruturada.
- Veiculo deve ser tabela filha de Item.
- A criacao nao deve ser barrada caso o veiculo estruturado nao seja encontrado.

### Historico Do Lead

Representa evento operacional ou de status relacionado a um lead.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Identidade do usuario quando aplicavel.
- Status anterior.
- Novo status.
- Descricao.
- Origem do evento.
- Data de criacao.

### Nota E Observacao Do Lead

Representam textos relacionados a um lead.

Regras definidas:

- Notas sao editaveis.
- Observacoes sao editaveis.
- Observacoes devem criar historico de alteracao.

### Tag Do Lead

Representa uma etiqueta associada a um lead.

Regras definidas:

- Tags sao cadastraveis, fixas e globais.
- Tags nao sao texto livre.
- Um lead nao pode ter tag duplicada do mesmo tipo.

### Template De Mensagem

Representa uma mensagem reutilizavel para comunicacao com cliente.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja quando especifico de loja.
- Nome exatamente igual ao aprovado na Meta.
- Idioma, com padrao `pt-BR`.
- Tipo.
- Conteudo.
- Indicador de ativo.
- Indicador de exclusao logica.
- Datas de criacao e atualizacao.

Regras definidas:

- Templates da empresa podem ser usados por todas as lojas da empresa.
- Templates da loja sao especificos daquela loja.
- Templates podem ser criados, editados, desativados ou apagados por gerente geral e `ADMIN`.
- Templates ja usados podem ser excluidos apenas de forma logica.
- Placeholders/componentes da Meta devem ser preenchidos automaticamente.

### Contato De WhatsApp

Representa um telefone de cliente identificado em eventos do WhatsApp e vinculado ao escopo de uma loja.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Identidade do lead quando encontrado.
- Telefone em formato aceito pelo WhatsApp/Meta.
- Nome de exibicao.
- Datas de criacao e atualizacao.

### Conversa

Representa o agrupamento de mensagens de WhatsApp para um contato e, quando houver, um lead.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Identidade do contato de WhatsApp.
- Identidade do lead quando encontrado.
- Identidade do dono responsavel quando houver.
- Datas de criacao e atualizacao.

Regras definidas:

- Conversas pertencem a uma loja.
- Conversas iniciadas pelo lead sem vendedor ficam disponiveis na fila da loja.
- Pre-venda assumir conversas da fila fica para segunda fase.
- Gerente responde conversa apenas se assumir o lead; caso contrario, supervisiona.
- Eventos de auditoria ficam registrados tecnicamente no MVP.

### Mensagem De Conversa

Representa mensagem recebida pelo webhook do WhatsApp ou registrada como saida pela plataforma.

Atributos conhecidos:

- Identidade.
- Identidade da conversa.
- Direcao.
- Tipo.
- Status.
- Identidade externa da mensagem quando informada pelo provedor.
- Conteudo textual ou legenda.
- Metadados de midia.
- Referencia de midia em S3 ou bucket equivalente.
- Payload bruto do evento quando aplicavel.
- Datas de criacao e atualizacao.

Regras definidas:

- Midias de WhatsApp devem ser armazenadas em S3 ou bucket equivalente.
- Dados de status da Meta devem ser salvos pelo sistema.

### Conta De E-Mail

Representa conta de e-mail usada para importar leads.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Nome.
- Host.
- Porta.
- Usuario.
- Senha criptografada.
- Protocolo.
- Indicador de SSL.
- Status.
- Data da ultima leitura.
- Datas de criacao e atualizacao.

Regras definidas:

- Contas de e-mail podem ser gerenciadas por `ADMIN` e gerente geral.
- Excluir conta preserva historico de importacao.
- Mensagens importadas devem ser marcadas como lidas.
- Devem ser preservados apenas dados do lead extraidos do e-mail.
- Falhas notificam administradores e importacoes com falha devem ser tentadas novamente.

### Solicitacao LGPD

Representa uma solicitacao de titular de dados.

Regras definidas:

- No MVP, solicitacoes sao registradas e tratadas manualmente por `ADMIN`.
- Automacoes irreversiveis ficam fora do MVP.
- Acoes, executor e data devem ser registrados.
- Sem prazo automatico de expurgo no MVP.

## Agregados

Candidatos atuais a agregados:

- Empresa com lojas como estrutura de tenant.
- Usuario como perfil de identidade, autorizacao e sessao.
- Lead como agregado comercial principal, com historico, notas, observacoes, tags, comunicacoes, telefones e relacoes de duplicidade.
- Item com veiculo estruturado.
- Conversa como agregado de atendimento do WhatsApp, com contato, mensagens, midias e eventos de status.
- Conta de e-mail como configuracao de integracao de captacao de leads.
- Solicitacao LGPD como agregado administrativo.

## Candidatos A Value Objects

- Numero de telefone E.164.
- Endereco de e-mail.
- Documento fiscal da loja.
- Dinheiro ou valor de venda com moeda.
- Escopo de tenant.
- Origem do lead.
- Status do lead.
- Papel do usuario.
- Nome aprovado de template Meta.

## Candidatos A Eventos De Dominio

- LeadCriado.
- LeadAtribuido.
- LeadAssumidoPorVendedor.
- StatusDoLeadAlterado.
- PrimeiroContatoDoLeadRegistrado.
- LeadReativadoPorRecontato.
- NovoLeadCriadoPorCliqueEmAnuncio.
- LeadMarcadoComoDuplicado.
- LeadEncaminhadoParaFAndI.
- LinkDeWhatsAppGerado.
- MensagemDeWhatsAppRecebida.
- MidiaDeWhatsAppArmazenada.
- StatusDeMensagemMetaRecebido.
- ConversaAssumida.
- LeadImportadoPorEmail.
- SolicitacaoLgpdRegistrada.

## Glossario

- Atribuicao: ato de definir usuario responsavel por um lead.
- Avaliador: papel formal do produto; permissoes especificas ainda precisam ser detalhadas.
- Conversa: agrupamento de mensagens de WhatsApp por contato/lead em uma loja.
- Duplicado: lead considerado uma oportunidade possivelmente repetida.
- Empresa: agrupador de lojas.
- F&I: usuario que atua em simulacao de banco e proposta aprovada.
- Follow-up: acao agendada relacionada a um lead; fica para segunda fase.
- Funil: conjunto de status que representa progresso comercial.
- Item: entidade pertencente ao usuario e relacionada ao veiculo.
- Lead: oportunidade comercial e representacao do cliente no MVP.
- Loja: unidade operacional vinculada a empresa.
- Pre-venda: usuario responsavel por gerar lead e fazer primeiro atendimento.
- SLA: expectativa de tempo para atribuicao ou primeiro contato; fica para segunda fase.
- Template: mensagem reutilizavel usada no contato com clientes.
- Vendedor: usuario responsavel pelo contato comercial e negociacao.
- Veiculo: entidade estruturada filha de Item.
