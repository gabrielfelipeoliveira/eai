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
