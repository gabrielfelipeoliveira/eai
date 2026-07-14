# Modelo de Dominio

Este documento descreve o dominio conceitual definido. Ele evita detalhes tecnicos de framework de proposito. Duvidas de modelagem e regras ainda nao decididas ficam em [Pendencias de produto](pendencias.md).

## Contextos de Dominio

O sistema atual pode ser entendido pelas seguintes areas de dominio:

- Identidade e acesso.
- Tenancy.
- Gestao de leads.
- Distribuicao de leads e SLA.
- Templates de comunicacao.
- Importacao de leads por e-mail.
- Agenda de follow-up.
- Dashboard e relatorios.

Os limites exatos dos contextos podem evoluir conforme o produto amadurecer.

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
- Datas de criacao e atualizacao.

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
- Identidade da loja.
- Status.
- Papeis.
- Datas de criacao e atualizacao.

### Lead

Representa uma oportunidade comercial de um cliente interessado em um veiculo ou servico.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Nome do cliente.
- Telefone do cliente.
- E-mail do cliente.
- Cidade do cliente.
- Veiculo de interesse.
- Origem.
- Mensagem original.
- Status.
- Usuario responsavel.
- Data de atribuicao.
- Datas de criacao e atualizacao.
- Data do primeiro contato.
- Data do ultimo contato.
- Motivo de perda.
- Valor de venda.

### Historico do Lead

Representa um evento operacional ou de status relacionado a um lead.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Identidade do usuario.
- Status anterior.
- Novo status.
- Descricao.
- Data de criacao.

### Nota do Lead

Representa uma nota adicionada a um lead por um usuario.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Identidade do usuario.
- Texto da nota.
- Data de criacao.

### Tag do Lead

Representa uma etiqueta associada a um lead.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Nome.

### Tarefa de Follow-Up

Representa uma acao futura relacionada a um lead.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Identidade do usuario.
- Titulo.
- Descricao.
- Data de vencimento.
- Data de conclusao.
- Status.
- Datas de criacao e atualizacao.

### Configuracao de Distribuicao de Leads

Representa como uma loja distribui leads.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Modo de distribuicao.
- Indicador de ativo.

### Politica de SLA de Lead

Representa regras de tempo para atribuicao e primeiro contato em uma loja.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Minutos para atribuir.
- Minutos para primeiro contato.
- Indicador de ativo.

### Template de Mensagem

Representa uma mensagem reutilizavel para comunicacao com cliente.

Atributos conhecidos:

- Identidade.
- Identidade da empresa.
- Identidade da loja.
- Nome.
- Tipo.
- Conteudo.
- Indicador de ativo.
- Datas de criacao e atualizacao.

### Comunicacao do Lead

Representa um artefato de comunicacao gerado pelo sistema para um lead.

Atributos conhecidos:

- Identidade.
- Identidade do lead.
- Identidade do usuario.
- Canal.
- Identidade do template.
- Mensagem.
- Data de criacao.

### Contato de WhatsApp

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
- Identidade do vendedor responsavel quando o lead tiver responsavel.
- Datas de criacao e atualizacao.

### Mensagem de Conversa

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

### Evento de Mensagem de Conversa

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

### Conta de E-mail

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

## Agregados

Candidatos atuais a agregados:

- Empresa com lojas como estrutura de tenant.
- Usuario como perfil de identidade e autorizacao.
- Lead como agregado comercial principal, com historico, notas, tags, comunicacoes e follow-ups como registros relacionados.
- Conversa como agregado de atendimento do WhatsApp, com contato, mensagens e eventos de status relacionados.
- Configuracao de distribuicao e politica de SLA como configuracoes operacionais da loja.
- Conta de e-mail como configuracao de integracao de captacao de leads.

## Candidatos a Value Objects

Conceitos atuais que podem se tornar value objects:

- Numero de telefone.
- Endereco de e-mail.
- Documento da empresa.
- Dinheiro ou valor de venda.
- Escopo de tenant.
- Duracao de SLA.
- Telefone de WhatsApp.
- Origem do lead.
- Status do lead.

## Candidatos a Eventos de Dominio

A implementacao atual registra historico, mas nao modela eventos de dominio explicitamente.

Eventos potenciais:

- LeadCriado.
- LeadAtribuido.
- StatusDoLeadAlterado.
- PrimeiroContatoDoLeadRegistrado.
- LeadMarcadoComoDuplicado.
- FollowUpCriado.
- FollowUpConcluido.
- FollowUpCancelado.
- LinkDeWhatsAppGerado.
- LeadImportadoPorEmail.
- SlaViolado.

## Glossario

- Conta: conta de integracao por e-mail usada para importacao de leads.
- Empresa: organizacao cliente que usa o EAI.
- Loja: unidade de concessionaria vinculada a uma empresa.
- Lead: oportunidade comercial de um potencial cliente.
- Vendedor: usuario responsavel pelo contato comercial e negociacao.
- Gestor: usuario responsavel pela supervisao operacional.
- Atribuicao: ato de definir um usuario responsavel por um lead.
- Funil: sequencia de status que representa o progresso comercial.
- SLA: expectativa de tempo para atribuicao ou primeiro contato.
- Follow-up: acao agendada relacionada a um lead.
- Template: mensagem reutilizavel usada no contato com clientes.
- Comunicacao: artefato registrado de contato com cliente.
- Duplicado: lead considerado uma oportunidade possivelmente repetida.
