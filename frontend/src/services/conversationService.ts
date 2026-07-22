import { api } from './api';
import type { ConversationMessage, ConversationMessageStatus, ConversationSummary } from '../types/message';

export interface ConversationFilters {
  sellerId?: string;
  messageStatus?: ConversationMessageStatus;
  startAt?: string;
  endAt?: string;
}

export async function listConversations(filters: ConversationFilters = {}) {
  const response = await api.get<ConversationSummary[]>('/conversations', { params: filters });
  return response.data;
}

export async function listConversationMessages(conversationId: string) {
  const response = await api.get<ConversationMessage[]>(`/conversations/${conversationId}/messages`);
  return response.data;
}

export async function sendConversationTextMessage(conversationId: string, content: string) {
  const response = await api.post<ConversationMessage>(`/conversations/${conversationId}/messages`, { content });
  return response.data;
}

export async function sendConversationMediaMessage(conversationId: string, file: File, caption?: string) {
  const formData = new FormData();
  formData.append('file', file);
  if (caption?.trim()) {
    formData.append('caption', caption.trim());
  }
  const response = await api.post<ConversationMessage>(`/conversations/${conversationId}/media`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
}

export async function downloadConversationMessageMedia(conversationId: string, messageId: string) {
  const response = await api.get<Blob>(`/conversations/${conversationId}/messages/${messageId}/media`, {
    responseType: 'blob',
  });
  return response.data;
}
