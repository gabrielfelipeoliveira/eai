# Perguntas De Negocio

Este arquivo resume respostas do Product Owner e decisoes de produto. As regras oficiais ficam em [Regras de negocio](business-rules.md). Pendencias abertas ficam em [Pendencias de produto](pendencias.md).

## 1. Escopo Do MVP

Status: confirmado.

O MVP inclui tenancy basico, papeis e permissoes iniciais, captacao de leads por WhatsApp e e-mail, gestao de leads, pipeline comercial, conversas de WhatsApp, templates, fluxo entre pre-venda, vendedor, F&I e avaliador, e LGPD basica com tratamento manual por `ADMIN`.

Ficam fora do MVP e para fase posterior distribuicao automatica, SLA, follow-ups, notificacoes, KPIs, dashboard gerencial completo, relatorios gerenciais, parsers dedicados, funil configuravel, tela de auditoria, escopo operacional de `AUDITOR`, automacoes irreversiveis de LGPD e politica formal de retencao.

## 2. Papeis De Usuario

Decisao:

- `ADMIN`: usuario global da plataforma.
- `MANAGER`: gerente geral com escopo da empresa.
- `STORE_MANAGER`: gerente de loja com escopo da loja.
- `SELLER`: vendedor; assume leads disponiveis e atende apenas seus leads.
- `PRE_SALES`: gera leads e faz primeiro atendimento.
- `F_AND_I`: participa de simulacao e proposta.
- `AVALIADOR`: papel formal do produto; permissoes especificas ainda devem ser detalhadas antes da implementacao.

Decisoes adicionais:

- `MANAGER` e `STORE_MANAGER` sao papeis separados.
- Cada usuario tem apenas um papel.
- `AUDITOR` fica fora do MVP.
- Somente `ADMIN` e gerente podem criar, editar, desativar ou remover usuarios.
- Usuarios podem editar o proprio perfil e redefinir sua senha.
- Deve existir acao administrativa para resetar senha.

## 3. Empresas, Lojas E Usuarios

Decisao:

- Empresa e agrupador das lojas.
- Loja e a unidade operacional.
- CNPJ, endereco, telefone e razao social pertencem a loja.
- Usuarios pertencem a uma empresa e, quando operacionais, a uma loja.
- Empresa nao pode ser desativada com usuarios ativos vinculados.
- Desativacao de empresa ou loja remove acesso dos usuarios relacionados, mas preserva dados e historico.

## 4. Autenticacao E Sessao

Decisao:

- Usuario inativo ve mensagem generica.
- Login em multiplas sessoes nao e permitido.
- Rotacao de refresh token revoga o anterior imediatamente.
- Sessao dura 30 dias.
- Logout revoga todas as sessoes.
- Desativacao de usuario revoga sessoes ativas.

## 5. Ciclo De Vida Do Lead

Decisao:

- O MVP usa os status atuais.
- `VISIT_SCHEDULED`, `SIMULATING` e `PROPOSAL_APPROVED` sao etapas opcionais.
- Etapas opcionais aparecem no pipeline, mas nao impoem ordem obrigatoria.
- Lead perdido ou vendido pode voltar a ficar ativo por recontato.
- Novo clique em anuncio cria novo lead com rastreio/historico relacionado ao contato anterior.
- Campos obrigatorios por anuncio: nome do carro, ano, modelo, valor, nome do lead e telefone WhatsApp.
- Depois do primeiro contato, podem ser alterados o carro/anuncio e o nome do lead.
- Lead pode ter outros numeros vinculados.
- Vendedor pode editar valor de venda ou motivo de perda somente com permissao especifica.

## 6. Visibilidade, Pesquisa E Atribuicao

Decisao:

- Ordenacao padrao de leads: por chegada.
- Filtros obrigatorios de tela nao sao relevantes para o MVP neste momento.
- Busca textual deve ser normalizada.
- Vendedores nao podem ver leads de outros vendedores.
- Atribuicao principal do MVP e manual.
- `PRE_SALES` gera lead; lead fica disponivel no pipeline.
- `SELLER` pode assumir lead disponivel.
- `MANAGER` e `STORE_MANAGER` podem atribuir leads conforme escopo.

## 7. Notas, Observacoes, Tags E Historico

Decisao:

- Notas e observacoes sao editaveis.
- Observacoes devem ter historico.
- Tags sao cadastraveis, fixas e globais.
- Um lead nao pode ter tag duplicada do mesmo tipo.
- Historico deve registrar status anterior, novo status, descricao, usuario e data quando aplicavel.
- Atores de sistema sao detalhe tecnico de auditoria e nao bloqueiam o MVP.

## 8. Pipeline

Decisao:

- Pipeline permite alteracao de status por arrastar e soltar.
- Nenhuma coluna exige dados adicionais antes de aceitar o lead no MVP.
- Diferenciacao visual de etapas opcionais e obrigatorias sera decidida na fase de UX.

## 9. WhatsApp, Templates E Conversas

Decisao:

- Cada loja tem apenas um numero WhatsApp.
- Conversas pertencem a uma loja e devem ter dono responsavel.
- Conversa sem vendedor fica na fila da loja.
- Gerente responde somente se assumir o lead.
- Eventos de auditoria ficam registrados tecnicamente no MVP; tela de auditoria fica para fase posterior.
- Midias WhatsApp devem ser armazenadas em S3 ou bucket equivalente.
- Gerar link conta como primeiro contato e links gerados sao imutaveis.
- Templates podem ser gerenciados por gerente geral e `ADMIN`.
- Templates usados podem ser excluidos apenas de forma logica.
- Idioma padrao sem `languageCode`: `pt-BR`.
- Nome do template no EAI deve ser exatamente o aprovado na Meta.
- Placeholders da Meta devem ser preenchidos automaticamente.
- Status de mensagem da Meta devem ser salvos.
- Telefone deve aceitar formatos suportados pelo WhatsApp/Meta.

## 10. Importacao De Leads Por E-Mail

Decisao:

- Leads podem entrar por e-mail de plataformas como Webmotors e iCarros, registradas como `LeadSource`.
- Duplicidade usa telefone/WhatsApp e loja.
- Cada chegada fica registrada no historico.
- Entrada duplicada fica na mesma conversa anterior, mas gera novo lead marcado como duplicado.
- Mensagens importadas devem ser marcadas como lidas.
- Devem ser preservados apenas os dados do lead extraidos do e-mail.
- Contas de e-mail podem ser gerenciadas por `ADMIN` e gerente geral.
- Excluir conta preserva historico de importacao.
- Testes com falha notificam administradores.
- Importacoes com falha devem ser tentadas novamente.

## 11. Dominio, Internacionalizacao E Dados Padrao

Decisao:

- Cliente e um lead.
- Veiculo deve ser entidade estruturada, mas sua ausencia nao deve barrar criacao.
- Veiculo deve ser tabela filha de Item.
- Item pertence ao usuario.
- Validacoes por pais devem considerar uso internacional.
- Telefone segue E.164.
- Valor de venda suporta moedas alem de BRL, com BRL como default.
- Seeds nao devem existir em deploys de producao.
- Dados de demonstracao devem ser separados de seeds obrigatorios.
- Templates padrao do MVP serao definidos depois.
- Templates padrao podem ser aprovados ou alterados por `ADMIN` e gerente geral.

## 12. Auditoria, Retencao E LGPD

Decisao:

- LGPD basica entra no MVP.
- No MVP, nao implementar automacoes irreversiveis.
- Solicitações LGPD devem ser registradas e tratadas manualmente por `ADMIN`.
- Exclusao nao deve ser sempre fisica.
- Sistema deve registrar solicitacao, executor, data e acao aplicada.
- Sem prazo automatico de expurgo no MVP.
- Validacao juridica, automacoes irreversiveis e politica formal de retencao ficam para fase posterior.
