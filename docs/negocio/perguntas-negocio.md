# Perguntas De Negocio

Este arquivo consolida respostas do Product Owner, decisoes de produto e poucas duvidas que ainda precisam de confirmacao.
O detalhamento completo de pendencias continua em [Pendencias de produto](pendencias.md).

## 1. Escopo Do MVP

Status: confirmado.

Escopo do MVP:

- Tenancy basico: empresa, lojas e usuarios.
- Papeis e permissoes iniciais: admin, gerente geral, gerente de loja, pre-venda, vendedor e F&I.
- Captacao de leads por WhatsApp e por e-mail.
- Gestao de leads com historico de origem, duplicidade por telefone e loja, e funil comercial.
- Conversas de WhatsApp por loja, com dono responsavel e vinculo com lead.
- Templates de mensagem globais da empresa e templates especificos da loja.
- Fluxo operacional entre pre-venda, vendedor e F&I.
- Pipeline com status atuais e etapas opcionais visiveis no MVP.
- Arquitetura preparada para etapas configuraveis no futuro.
- LGPD basica: processo para exclusao, anonimizacao ou bloqueio de dados pessoais quando aplicavel.

Fica para segunda fase:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- KPIs, dashboards e relatorios gerenciais.
- Parsers dedicados para plataformas especificas.
- Configuracao de etapas do funil por empresa ou loja.
- Regras avancadas de auditoria, retencao e compliance.

## 2. Papeis De Usuario

Decisao:

- `ADMIN`: usuario global da plataforma, com acesso completo.
- `MANAGER`: gerente geral, com acesso a todas as lojas da empresa, todas as conversas, atribuicao de leads e graficos de gestao.
- `STORE_MANAGER`: gerente de loja, com acesso gerencial limitado a loja em que esta alocado.
- `SELLER`: pode visualizar leads disponiveis, assumir leads e acessar as conversas dos leads sob sua responsabilidade.
- `PRE_SALES`: gera o lead, faz o primeiro atendimento e envia templates iniciais.
- `F_AND_I`: atua nas etapas de simulacao e proposta, principalmente na simulacao de banco e na proposta aprovada.

Decisoes adicionais:

- `MANAGER` e `STORE_MANAGER` sao papeis separados.
- Cada usuario tem apenas um papel.
- `AUDITOR` deve ser removido do escopo do MVP.

## 3. Escopo Por Empresa, Loja E Usuario

Decisao:

- Empresa e a entidade principal.
- Uma empresa pode ter varias lojas independentes.
- Usuarios pertencem a uma empresa.
- Em regra, usuarios ficam alocados em uma loja.
- `ADMIN` e global da plataforma.
- `MANAGER` pode estar vinculado apenas a empresa e visualizar todas as lojas dessa empresa.
- Usuarios operacionais, como `STORE_MANAGER`, `SELLER`, `PRE_SALES` e `F_AND_I`, devem operar dentro da loja em que estao alocados.

## 4. Ciclo De Vida Do Lead

Decisao:

- O MVP deve iniciar com os status ja existentes no sistema.
- `SIMULATING`, `PROPOSAL_APPROVED` e `VISIT_SCHEDULED` sao etapas opcionais do funil.
- As etapas opcionais devem aparecer visualmente no pipeline do MVP.
- `SIMULATING` representa a etapa em que o F&I faz a simulacao de banco.
- `PROPOSAL_APPROVED` representa uma proposta mais firme, pronta para apresentar ao cliente.
- `SIMULATING` e `PROPOSAL_APPROVED` ficam conceitualmente entre negociacao e proposta enviada, mas nao devem impor uma ordem obrigatoria.
- Nenhum status atual precisa ser ocultado no MVP.
- A arquitetura deve estar preparada para status configuraveis em fase futura.

## 5. Atribuicao De Leads

Decisao:

- A atribuicao principal do MVP deve ser manual.
- Pre-venda gera o lead; depois disso, o lead aparece como disponivel no pipeline.
- Leads disponiveis podem ser assumidos por vendedores.
- `MANAGER` pode atribuir um lead diretamente a uma pessoa.
- `STORE_MANAGER` pode atribuir leads dentro da propria loja.
- Distribuicao automatica fica para segunda fase.
- `F_AND_I` participa das etapas de simulacao e proposta, mas nao substitui o dono comercial principal do lead.

## 6. SLA, Follow-Ups E Notificacoes

Decisao:

- SLA, follow-ups e notificacoes ficam para segunda fase.
- O MVP nao deve depender dessas regras para funcionar.
- Qualquer campo ou tela relacionada a SLA/follow-up deve ser tratada como apoio, sem regra operacional obrigatoria.

## 7. WhatsApp, Templates E Conversas

Decisao:

- Cada loja deve ter apenas um numero de WhatsApp.
- O numero de WhatsApp pertence a uma loja.
- Templates podem pertencer a empresa ou a uma loja.
- Templates da empresa podem ser usados por todas as lojas da empresa.
- Templates da loja sao especificos daquela loja.
- Conversas pertencem a uma loja.
- Cada conversa deve ter um dono responsavel.
- Quando uma conversa for iniciada pelo lead e ainda nao tiver vendedor, ela fica disponivel na fila da loja.
- Pre-venda assumir conversas da fila fica para segunda fase.
- Gerente pode responder uma conversa somente se assumir o lead.
- Enquanto o lead estiver no nome de um vendedor, o gerente apenas supervisiona.
- O lead representa o outro lado da conversa.
- Nas etapas de simulacao e proposta, o lead pode estar relacionado ao vendedor e tambem ao F&I.

## 8. Importacao De Leads Por E-Mail

Decisao:

- Leads podem entrar diretamente pelo WhatsApp, especialmente trafego pago.
- Leads tambem podem entrar por e-mail vindo de plataformas como Webmotors e iCarros, registradas como `LeadSource`.
- Duplicidade deve ser validada por telefone/WhatsApp e loja.
- Quando um lead repetido chegar, o sistema deve indicar duplicidade.
- O mesmo lead pode chegar varias vezes; cada chegada deve ficar registrada no historico.
- Quando uma nova entrada for duplicada, ela fica na mesma conversa anterior, mas gera um novo lead com indicacao de duplicado.
- O lead duplicado deve iniciar com dados proprios, preservando o historico da nova chegada e a relacao com o lead/conversa anterior.

## 9. KPIs E Relatorios

Decisao:

- KPIs e relatorios ficam para segunda fase.
- O MVP pode ter visoes operacionais simples, mas nao deve bloquear entrega por dashboard gerencial completo.

## 10. Auditoria, Retencao E LGPD

Decisao:

- O sistema deve considerar LGPD desde o MVP.
- Na fase inicial, apenas `ADMIN` pode executar solicitacoes LGPD.
- Deve existir um fluxo administrativo para atender solicitacoes de titulares, incluindo acesso, correcao, bloqueio, anonimizacao ou eliminacao quando aplicavel.
- A exclusao nao deve ser sempre fisica.
- O sistema deve registrar a solicitacao LGPD, quem executou, quando executou e qual acao foi aplicada.
- Dados anonimizados nao devem permitir reidentificacao razoavel do titular.

Direcao recomendada para implementacao:

- Anonimizar dados pessoais de leads, conversas, mensagens e historicos quando a manutencao operacional, estatistica ou de auditoria ainda for necessaria.
- Eliminar dados pessoais quando nao houver base legal, obrigacao de retencao ou necessidade operacional.
- Bloquear tratamento quando houver disputa, revisao, solicitacao pendente ou necessidade de congelar o uso dos dados.
- Manter registros minimos de auditoria quando forem necessarios para cumprimento legal, defesa em processo, prevencao a fraude ou prestacao de contas.

Campos pessoais candidatos a anonimizacao:

- Lead: nome, telefone, e-mail, documento, mensagem original, observacoes livres e qualquer dado pessoal em campos de interesse.
- Conversa e mensagens: telefone, nome exibido, conteudo textual, midias, metadados que identifiquem a pessoa e payload bruto do provedor.
- Usuario: nome, e-mail, telefone e dados de perfil que identifiquem pessoa fisica, quando o usuario solicitar exclusao e nao houver obrigacao de manter.
- Auditoria: manter identificadores tecnicos minimos quando necessario, evitando expor dados pessoais em texto livre.

Retencao recomendada para MVP:

- Sem prazo automatico de expurgo no MVP.
- Atender solicitacoes LGPD caso a caso via fluxo administrativo.
- Registrar a decisao aplicada em cada solicitacao.
- Deixar a politica de prazos automatizados para fase posterior, com validacao juridica.

Duvidas restantes:

- Validar a direcao LGPD com assessoria juridica antes de implementar automacoes irreversiveis.
- Segunda fase

## Como Usar

- Use este arquivo como resumo das respostas de negocio.
- Registre decisoes definitivas nos documentos oficiais de negocio.
- Mantenha perguntas detalhadas e pendencias abertas em `docs/negocio/pendencias.md`.
- Nao implemente regra de negocio enquanto a decisao nao estiver documentada.
