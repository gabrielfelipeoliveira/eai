export type MessageTemplateType = 'FIRST_CONTACT' | 'FOLLOW_UP' | 'VISIT_INVITE' | 'PROPOSAL' | 'NO_RESPONSE' | 'SOLD' | 'LOST';

export type MessageTemplateMetaStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'PAUSED' | 'DISABLED';

export type LeadCommunicationChannel = 'WHATSAPP_LINK' | 'WHATSAPP_TEMPLATE';

export type ConversationMessageDirection = 'INBOUND' | 'OUTBOUND';

export type ConversationMessageType = 'TEXT' | 'TEMPLATE' | 'IMAGE' | 'AUDIO' | 'DOCUMENT';

export type ConversationMessageStatus = 'RECEIVED' | 'SENT' | 'DELIVERED' | 'READ' | 'FAILED';

export interface MessageTemplate {
  id: string;
  companyId: string;
  storeId: string | null;
  name: string;
  type: MessageTemplateType;
  content: string;
  languageCode: string;
  metaStatus: MessageTemplateMetaStatus;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

export interface LeadCommunication {
  id: string;
  leadId: string;
  userId: string;
  channel: LeadCommunicationChannel;
  templateId: string;
  message: string;
  createdAt: string;
}

export interface WhatsappLink {
  leadId: string;
  templateId: string;
  communicationId: string;
  message: string;
  url: string;
}

export interface WhatsAppTemplateSendResponse {
  leadId: string;
  templateId: string;
  communicationId: string;
  conversationMessageId: string;
  status: ConversationMessageStatus;
  externalMessageId: string | null;
  message: string;
  providerResponse: string | null;
}

export interface ConversationSummary {
  id: string;
  companyId: string;
  storeId: string;
  contactId: string;
  leadId: string | null;
  responsibleUserId: string | null;
  leadName: string | null;
  phone: string;
  contactDisplayName: string | null;
  lastMessageId: string | null;
  lastMessageDirection: ConversationMessageDirection | null;
  lastMessageType: ConversationMessageType | null;
  lastMessageStatus: ConversationMessageStatus | null;
  lastMessageContent: string | null;
  lastInteractionAt: string;
  unreadCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ConversationMessage {
  id: string;
  conversationId: string;
  direction: ConversationMessageDirection;
  type: ConversationMessageType;
  status: ConversationMessageStatus;
  externalMessageId: string | null;
  content: string | null;
  mediaId: string | null;
  mediaMimeType: string | null;
  createdAt: string;
  updatedAt: string;
}
