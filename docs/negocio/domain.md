# Modelo De Dominio

Este documento descreve o dominio conceitual definido. Ele evita detalhes tecnicos de framework. Duvidas de modelagem e regras ainda nao decididas ficam em [Pendencias de produto](pendencias.md).

## Contextos De Dominio

O sistema atual pode ser entendido pelas seguintes areas de dominio:

- Identidade e acesso.
- Tenancy.
- Gestao de leads.
- Atribuicao manual de leads.
- Pipeline comercial.
- Conversas de WhatsApp.
- Templates de comunicacao.
- Importacao de leads por e-mail.
- LGPD, auditoria e retencao.

Ficam para segunda fase:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- KPIs, dashboards e relatorios gerenciais.
- Configuracao de etapas do funil por empresa ou loja.

## Entidades Principais

### Empresa

Representa uma organizacao cliente que usa o EAI.

Atributos conhecidos:

- Identidade.
- Nome.
- Documento.
- E-mail.
- Telefone.
- Status.
- Datas de criacao e atualizacao.

### Loja

Representa uma loja ou unidade de concessionaria vinculada a uma empresa.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Nome.
- Cidade.
- Estado.
- Status.
- Numero de WhatsApp da loja.
- Datas de criacao e atualizacao.

Regra definida:

- Cada loja deve ter apenas um numero de WhatsApp.

### Usuario

Representa uma pessoa que autentica e opera no sistema.

Atributos conhecidos:

- Identidade.
- Nome.
- E-mail.
- Hash da senha.
- Telefone.
- Cargo.
- Identidade da empresa quando aplicavel.
- Identidade da loja quando aplicavel.
- Status.
- Papel unico.
- Datas de criacao e atualizacao.

Papeis do MVP:

- `ADMIN`.
- `MANAGER`.
- `STORE_MANAGER`.
- `SELLER`.
- `PRE_SALES`.
- `F_AND_I`.

Regras definidas:

- `ADMIN` e global da plataforma.
- `MANAGER` pertence a uma empresa e pode visualizar todas as lojas dessa empresa.
- Usuarios operacionais devem operar dentro da loja em que estao alocados.
- `RECEPTIONIST` e `AUDITOR` ficam fora do MVP.

### Lead

Representa uma oportunidade comercial de um cliente interessado em um veiculo ou servico.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Nome do cliente.
- Telefone ou WhatsApp do cliente.
- E-mail do cliente.
- Cidade do cliente.
- Veiculo de interesse.
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
- Valor de venda.
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

- `VISIT_SCHEDULED`, `SIMULATING` e `PROPOSAL_APPROVED` sao etapas opcionais.
- As etapas opcionais devem aparecer visualmente no pipeline do MVP.
- `SIMULATING` e `PROPOSAL_APPROVED` ficam conceitualmente entre negociacao e proposta enviada, sem ordem obrigatoria.
- A arquitetura deve estar preparada para status configuraveis em fase futura.
- Duplicidade e validada por telefone/WhatsApp e loja.
- Lead duplicado inicia com dados proprios, mas preserva historico da nova chegada e relacao com lead/conversa anterior.

### Historico Do Lead

Representa um evento operacional ou de status relacionado a um lead.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Identidade do usuario quando aplicavel.
- Status anterior.
- Novo status.
- Descricao.
- Origem do evento.
- Data de criacao.

### Nota Do Lead

Representa uma nota adicionada a um lead por um usuario.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Identidade do usuario.
- Texto da nota.
- Data de criacao.

### Tag Do Lead

Representa uma etiqueta associada a um lead.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Nome.

### Template De Mensagem

Representa uma mensagem reutilizavel para comunicacao com cliente.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja quando especifico de loja.
- Nome.
- Tipo.
- Conteudo.
- Indicador de ativo.
- Datas de criacao e atualizacao.

Regras definidas:

- Templates da empresa podem ser usados por todas as lojas da empresa.
- Templates da loja sao especificos daquela loja.

### Comunicacao Do Lead

Representa um artefato de comunicacao gerado pelo sistema para um lead.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Identidade do usuario.
- Canal.
- Identidade do template.
- Mensagem.
- Data de criacao.

### Contato De WhatsApp

Representa um telefone de cliente identificado em eventos do WhatsApp e vinculado ao escopo de uma loja.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Identidade do lead quando encontrado.
- Telefone.
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

### Mensagem De Conversa

Representa uma mensagem recebida pelo webhook do WhatsApp ou registrada como saida pela plataforma.

Atributos conhecidos:

- Identidade.
- Identidade da conversa.
- Direcao: entrada ou saida.
- Tipo: texto, template, imagem, audio ou documento.
- Status.
- Identidade externa da mensagem quando informada pelo provedor.
- Conteudo textual ou legenda.
- Metadados de midia quando informados.
- Payload bruto do evento quando aplicavel.
- Datas de criacao e atualizacao.

### Evento De Mensagem De Conversa

Representa um evento de status recebido do provedor para uma mensagem de conversa.

Atributos conhecidos:

- Identidade.
- Identidade da mensagem quando encontrada.
- Identidade externa da mensagem.
- Status informado pelo provedor.
- Motivo de falha quando informado.
- Payload bruto do evento.
- Data do evento.
- Data de criacao.

### Conta De E-Mail

Representa uma conta de e-mail usada para importar leads.

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

### Tarefa De Follow-Up

Representa uma acao futura relacionada a um lead.

Observacao:

- SLA, follow-ups e notificacoes ficam para segunda fase. A entidade pode existir no sistema atual, mas nao deve ser regra obrigatoria do MVP.

### Configuracao De Distribuicao De Leads

Representa como uma loja distribui leads.

Observacao:

- Distribuicao automatica fica para segunda fase. No MVP, a atribuicao principal e manual.

### Politica De SLA De Lead

Representa regras de tempo para atribuicao e primeiro contato em uma loja.

Observacao:

- SLA fica para segunda fase. No MVP, nao deve bloquear a operacao.

## Agregados

Candidatos atuais a agregados:

- Empresa com lojas como estrutura de tenant.
- Usuario como perfil de identidade e autorizacao.
- Lead como agregado comercial principal, com historico, notas, tags, comunicacoes e relacoes de duplicidade.
- Conversa como agregado de atendimento do WhatsApp, com contato, mensagens e eventos de status relacionados.
- Conta de e-mail como configuracao de integracao de captacao de leads.
- Configuracao de distribuicao e politica de SLA como configuracoes operacionais futuras da loja.

## Candidatos A Value Objects

Conceitos atuais que podem se tornar value objects:

- Numero de telefone.
- Endereco de e-mail.
- Documento da empresa.
- Dinheiro ou valor de venda.
- Escopo de tenant.
- Telefone de WhatsApp.
- Origem do lead.
- Status do lead.
- Papel do usuario.

## Candidatos A Eventos De Dominio

A implementacao atual registra historico, mas nao modela eventos de dominio explicitamente.

Eventos potenciais:

- LeadCriado.
- LeadAtribuido.
- LeadAssumidoPorVendedor.
- StatusDoLeadAlterado.
- PrimeiroContatoDoLeadRegistrado.
- LeadMarcadoComoDuplicado.
- LeadEncaminhadoParaFAndI.
- LinkDeWhatsAppGerado.
- MensagemDeWhatsAppRecebida.
- ConversaAssumida.
- LeadImportadoPorEmail.
- SolicitacaoLgpdRegistrada.
- DadosPessoaisAnonimizados.

## Glossario

- Conta: conta de integracao por e-mail usada para importacao de leads.
- Empresa: organizacao cliente que usa o EAI.
- Loja: unidade de concessionaria vinculada a uma empresa.
- Lead: oportunidade comercial de um potencial cliente.
- Vendedor: usuario responsavel pelo contato comercial e negociacao.
- Pre-venda: usuario responsavel por gerar o lead, fazer primeiro atendimento e enviar templates iniciais.
- F&I: usuario que atua em simulacao de banco e proposta aprovada.
- Gestor: usuario responsavel pela supervisao operacional.
- Atribuicao: ato de definir um usuario responsavel por um lead.
- Funil: conjunto de status que representa o progresso comercial.
- SLA: expectativa de tempo para atribuicao ou primeiro contato; fica para segunda fase.
- Follow-up: acao agendada relacionada a um lead; fica para segunda fase.
- Template: mensagem reutilizavel usada no contato com clientes.
- Comunicacao: artefato registrado de contato com cliente.
- Duplicado: lead considerado uma oportunidade possivelmente repetida.
