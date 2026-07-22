import { api } from './api';
import type { LeadCommunication, MessageTemplate, MessageTemplateMetaStatus, MessageTemplateType, WhatsAppTemplateSendResponse, WhatsappLink } from '../types/message';

export interface MessageTemplatePayload {
  companyId: string;
  storeId: string | null;
  name: string;
  type: MessageTemplateType;
  content: string;
  languageCode: string;
  metaStatus: MessageTemplateMetaStatus;
  active: boolean;
}

export async function listTemplates() {
  const response = await api.get<MessageTemplate[]>('/templates');
  return response.data;
}

export async function listActiveTemplates() {
  const response = await api.get<MessageTemplate[]>('/templates/active');
  return response.data;
}

export async function createTemplate(payload: MessageTemplatePayload) {
  const response = await api.post<MessageTemplate>('/templates', payload);
  return response.data;
}

export async function updateTemplate(id: string, payload: MessageTemplatePayload) {
  const response = await api.put<MessageTemplate>(`/templates/${id}`, payload);
  return response.data;
}

export async function deleteTemplate(id: string) {
  await api.delete(`/templates/${id}`);
}

export async function generateWhatsappLink(leadId: string, templateId: string) {
  const response = await api.post<WhatsappLink>(`/leads/${leadId}/whatsapp-link`, { templateId });
  return response.data;
}

export async function sendWhatsappTemplate(leadId: string, templateId: string) {
  const response = await api.post<WhatsAppTemplateSendResponse>(`/leads/${leadId}/whatsapp-template`, { templateId });
  return response.data;
}

export async function listLeadCommunications(leadId: string) {
  const response = await api.get<LeadCommunication[]>(`/leads/${leadId}/communications`);
  return response.data;
}
