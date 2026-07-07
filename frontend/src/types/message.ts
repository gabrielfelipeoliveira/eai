export type MessageTemplateType = 'FIRST_CONTACT' | 'FOLLOW_UP' | 'VISIT_INVITE' | 'PROPOSAL' | 'NO_RESPONSE' | 'SOLD' | 'LOST';

export type LeadCommunicationChannel = 'WHATSAPP_LINK';

export interface MessageTemplate {
  id: string;
  companyId: string;
  storeId: string;
  name: string;
  type: MessageTemplateType;
  content: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
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
