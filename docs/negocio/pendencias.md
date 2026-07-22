# Pendencias De Produto

Este documento centraliza duvidas de negocio ainda abertas. Regras ja definidas ficam em [Regras de negocio](business-rules.md).

## Como Usar

- Antes de implementar funcionalidade de negocio, revise este documento.
- Quando uma pergunta for respondida, atualize a documentacao de negocio afetada.
- Nao implemente comportamento baseado em suposicao.

## Pendencias Atuais

- Definir qual permissao especifica permite que `SELLER` edite valor de venda ou motivo de perda do lead. A regra de negocio exige permissao especifica, mas ainda nao define nome, papel autorizador, escopo ou comportamento de excecao.

## Itens Planejados Para Fase Posterior

Os itens abaixo nao bloqueiam o MVP, mas precisam de detalhamento antes de serem implementados:

- Distribuicao automatica de leads.
- SLA, follow-ups e notificacoes.
- KPIs, dashboard gerencial completo e relatorios gerenciais.
- Parsers dedicados para plataformas especificas.
- Configuracao de etapas do funil por empresa ou loja.
- Tela de auditoria.
- Papel/escopo operacional de `AUDITOR` fica fora do MVP.
- Eventos que criam notificacoes, auditoria ou relatorios.
- Automacoes irreversiveis de LGPD, com validacao juridica previa.
- Politica formal de prazos de retencao por tipo de dado.
- Templates padrao definitivos do MVP.
- Permissoes especificas detalhadas do papel `AVALIADOR`.

## Referencias

- [Regras de negocio](business-rules.md)
- [Decisoes consolidadas do Trello](decisoes-consolidadas-trello.md)
