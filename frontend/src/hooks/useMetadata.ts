import { useQuery } from '@tanstack/react-query';
import { getMetadata } from '../services/metadataService';
import type { MetadataCatalog, MetadataCollection, MetadataOption } from '../types/metadata';

const metadataStaleTime = 24 * 60 * 60 * 1000;

export type MetadataColor = 'default' | 'primary' | 'secondary' | 'success' | 'warning' | 'error' | 'info';

export const fallbackMetadata: MetadataCatalog = {
  locale: 'pt-BR',
  leadStatuses: [
    option('NEW', 'lead.status.new', 'Novo', 1, 'info'),
    option('AVAILABLE', 'lead.status.available', 'Disponivel', 2, 'primary'),
    option('ASSIGNED', 'lead.status.assigned', 'Atribuido', 3, 'secondary'),
    option('FIRST_CONTACT', 'lead.status.first_contact', 'Primeiro contato', 4, 'warning'),
    option('IN_NEGOTIATION', 'lead.status.in_negotiation', 'Em negociacao', 5, 'warning'),
    option('VISIT_SCHEDULED', 'lead.status.visit_scheduled', 'Visita agendada', 6, 'info'),
    option('PROPOSAL_SENT', 'lead.status.proposal_sent', 'Proposta enviada', 7, 'secondary'),
    option('SOLD', 'lead.status.sold', 'Vendido', 8, 'success'),
    option('LOST', 'lead.status.lost', 'Perdido', 9, 'error'),
    option('DUPLICATED', 'lead.status.duplicated', 'Duplicado', 10, 'default'),
  ],
  leadSources: [
    option('MANUAL', 'lead.source.manual', 'Manual', 1, 'default'),
    option('EMAIL', 'lead.source.email', 'E-mail', 2, 'default'),
    option('WEBSITE', 'lead.source.website', 'Site', 3, 'default'),
    option('FACEBOOK', 'lead.source.facebook', 'Facebook', 4, 'default'),
    option('INSTAGRAM', 'lead.source.instagram', 'Instagram', 5, 'default'),
    option('WEBMOTORS', 'lead.source.webmotors', 'Webmotors', 6, 'default'),
    option('ICARROS', 'lead.source.icarros', 'iCarros', 7, 'default'),
    option('OLX', 'lead.source.olx', 'OLX', 8, 'default'),
    option('API', 'lead.source.api', 'API', 9, 'default'),
  ],
  followUpStatuses: [
    option('PENDING', 'follow_up.status.pending', 'Pendente', 1, 'warning'),
    option('DONE', 'follow_up.status.done', 'Concluido', 2, 'success'),
    option('CANCELED', 'follow_up.status.canceled', 'Cancelado', 3, 'default'),
    option('OVERDUE', 'follow_up.status.overdue', 'Atrasado', 4, 'error'),
  ],
  userRoles: [
    option('ADMIN', 'user.role.admin', 'Administrador', 1, 'error'),
    option('MANAGER', 'user.role.manager', 'Gerente', 2, 'primary'),
    option('STORE_MANAGER', 'user.role.store_manager', 'Gerente de loja', 3, 'primary'),
    option('SELLER', 'user.role.seller', 'Vendedor', 4, 'success'),
    option('PRE_SALES', 'user.role.pre_sales', 'Pre-venda', 5, 'info'),
    option('F_AND_I', 'user.role.f_and_i', 'F&I', 6, 'secondary'),
    option('AUDITOR', 'user.role.auditor', 'Auditoria', 7, 'secondary'),
  ],
  userStatuses: [
    option('ACTIVE', 'user.status.active', 'Ativo', 1, 'success'),
    option('INACTIVE', 'user.status.inactive', 'Inativo', 2, 'default'),
  ],
  tenantStatuses: [
    option('ACTIVE', 'tenant.status.active', 'Ativo', 1, 'success'),
    option('INACTIVE', 'tenant.status.inactive', 'Inativo', 2, 'default'),
  ],
  messageTemplateTypes: [
    option('FIRST_CONTACT', 'message_template.type.first_contact', 'Primeiro contato', 1, 'info'),
    option('FOLLOW_UP', 'message_template.type.follow_up', 'Follow-up', 2, 'warning'),
    option('VISIT_INVITE', 'message_template.type.visit_invite', 'Convite para visita', 3, 'info'),
    option('PROPOSAL', 'message_template.type.proposal', 'Proposta', 4, 'secondary'),
    option('NO_RESPONSE', 'message_template.type.no_response', 'Sem resposta', 5, 'default'),
    option('SOLD', 'message_template.type.sold', 'Venda concluida', 6, 'success'),
    option('LOST', 'message_template.type.lost', 'Lead perdido', 7, 'error'),
  ],
  leadDistributionModes: [
    option('MANUAL', 'lead_distribution.mode.manual', 'Manual', 1, 'default'),
    option('ROUND_ROBIN', 'lead_distribution.mode.round_robin', 'Rodizio', 2, 'primary'),
    option('LEAST_BUSY', 'lead_distribution.mode.least_busy', 'Menor carteira', 3, 'secondary'),
  ],
  emailAccountStatuses: [
    option('NEVER_SYNCED', 'email_account.status.never_synced', 'Nunca sincronizada', 1, 'default'),
    option('SUCCESS', 'email_account.status.success', 'Sincronizada', 2, 'success'),
    option('FAILED', 'email_account.status.failed', 'Falhou', 3, 'error'),
  ],
  emailProtocols: [
    option('IMAP', 'email_account.protocol.imap', 'IMAP', 1, 'default'),
  ],
  conversationMessageDirections: [
    option('INBOUND', 'conversation.message.direction.inbound', 'Entrada', 1, 'info'),
    option('OUTBOUND', 'conversation.message.direction.outbound', 'Saida', 2, 'success'),
  ],
  conversationMessageTypes: [
    option('TEXT', 'conversation.message.type.text', 'Texto', 1, 'default'),
    option('TEMPLATE', 'conversation.message.type.template', 'Template', 2, 'primary'),
    option('IMAGE', 'conversation.message.type.image', 'Imagem', 3, 'info'),
    option('AUDIO', 'conversation.message.type.audio', 'Audio', 4, 'secondary'),
    option('DOCUMENT', 'conversation.message.type.document', 'Documento', 5, 'warning'),
  ],
  conversationMessageStatuses: [
    option('RECEIVED', 'conversation.message.status.received', 'Recebida', 1, 'info'),
    option('SENT', 'conversation.message.status.sent', 'Enviada', 2, 'success'),
    option('DELIVERED', 'conversation.message.status.delivered', 'Entregue', 3, 'success'),
    option('READ', 'conversation.message.status.read', 'Lida', 4, 'primary'),
    option('FAILED', 'conversation.message.status.failed', 'Falhou', 5, 'error'),
  ],
};

export function useMetadata(locale = 'pt-BR') {
  const query = useQuery({
    gcTime: 7 * metadataStaleTime,
    queryKey: ['metadata', locale],
    queryFn: () => getMetadata(locale),
    staleTime: metadataStaleTime,
  });

  const catalog = query.data ?? fallbackMetadata;

  function options(collection: MetadataCollection) {
    return catalog[collection].slice().sort((left, right) => left.order - right.order);
  }

  function find(collection: MetadataCollection, code: string | null | undefined) {
    if (!code) {
      return undefined;
    }
    return catalog[collection].find((item) => item.code === code);
  }

  function label(collection: MetadataCollection, code: string | null | undefined) {
    return find(collection, code)?.label ?? code ?? '-';
  }

  function color(collection: MetadataCollection, code: string | null | undefined) {
    return toMetadataColor(find(collection, code)?.color);
  }

  return { catalog, color, find, label, options, query };
}

function option(code: string, labelKey: string, label: string, order: number, color: string): MetadataOption {
  return { code, labelKey, label, order, color };
}

function toMetadataColor(color: string | undefined): MetadataColor {
  if (
    color === 'primary' ||
    color === 'secondary' ||
    color === 'success' ||
    color === 'warning' ||
    color === 'error' ||
    color === 'info'
  ) {
    return color;
  }
  return 'default';
}
