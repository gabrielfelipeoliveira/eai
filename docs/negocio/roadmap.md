# Roadmap

Este roadmap organiza a evolucao conhecida do projeto. Regras aprovadas ficam em [Regras de negocio](business-rules.md), e duvidas abertas ficam em [Pendencias de produto](pendencias.md).

## Sprint 0: Base De Engenharia

Objetivo:

- Estabelecer documentacao profissional e acordos de trabalho como fonte oficial da verdade.

Escopo:

- Criar e atualizar documentacao de produto, regras de negocio, dominio, casos de uso, arquitetura, API, banco e roadmap.
- Criar estrutura de ADR e decisoes arquiteturais iniciais.
- Criar documentos de onboarding em `.agents/` para agentes de IA.
- Preservar documentacao util existente.
- Registrar decisoes de negocio pendentes em vez de assumir comportamento.

Fora de escopo:

- Novas funcionalidades.
- Migrations de banco.
- Mudancas de API.
- Grandes refactors.

## MVP Confirmado

Escopo:

- Autenticacao e autorizacao.
- Sessao unica por usuario, refresh token rotativo, logout global e sessao de 30 dias.
- Tenancy basico: empresas, lojas e usuarios.
- Empresa como agrupador de lojas.
- Loja como unidade operacional com CNPJ, endereco, telefone e razao social.
- Papeis iniciais: `ADMIN`, `MANAGER`, `STORE_MANAGER`, `SELLER`, `PRE_SALES`, `F_AND_I` e `AVALIADOR`.
- Captacao de leads por WhatsApp e por e-mail.
- Cadastro, listagem e atualizacao de leads.
- Atribuicao manual de leads.
- Leads disponiveis para vendedores assumirem.
- Gerente geral atribuindo leads na empresa.
- Gerente de loja atribuindo leads na propria loja.
- Pipeline comercial com status atuais, etapas opcionais visiveis e alteracao por arrastar e soltar.
- Etapas de F&I: simulacao e proposta aprovada.
- Historico de origem, status, notas, observacoes, tags e duplicidade.
- Tags globais, fixas e cadastraveis.
- Busca textual normalizada.
- Conversas de WhatsApp por loja.
- Templates WhatsApp da empresa ou loja.
- Armazenamento de midias WhatsApp em S3 ou bucket equivalente.
- Importacao de leads por e-mail com historico e retentativas.
- LGPD basica com tratamento manual por `ADMIN`.

Objetivos:

- Isolar escopo de tenant conforme papeis definidos.
- Consolidar o fluxo de pre-venda, vendedor, F&I e avaliador.
- Preparar arquitetura de pipeline para etapas configuraveis.
- Organizar fila de conversas da loja.
- Fortalecer historico de origem e duplicidade de leads.
- Implementar fluxo administrativo LGPD basico.

## Versao 2

Temas candidatos:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- Relatorios gerenciais e KPIs.
- Dashboard gerencial completo.
- Parsers dedicados para Webmotors, iCarros e outras origens.
- Configuracao de funil por empresa ou loja.
- Tela de auditoria.
- Papel/escopo operacional de `AUDITOR` fica fora do MVP.
- Eventos que criam notificacoes, auditoria ou relatorios.
- Regras avancadas de auditoria e retencao.
- Automacoes irreversiveis de LGPD com validacao juridica.
- Politica formal de prazos de retencao por tipo de dado.
- Ferramentas de importacao e exportacao.
- Permissoes avancadas.
- Templates padrao definitivos do MVP, quando ainda nao definidos.

## Ideias Futuras

Ideias ainda nao aprovadas para implementacao:

- Aplicativo mobile.
- BI avancado.
- Qualificacao de leads assistida por IA.
- Coaching automatico para vendedores.
- Billing de assinatura.
- Automacao avancada de operacao comercial.
