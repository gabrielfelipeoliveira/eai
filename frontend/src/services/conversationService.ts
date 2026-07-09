import { api } from './api';
import type { ConversationMessage, ConversationSummary } from '../types/message';

export async function listConversations() {
  const response = await api.get<ConversationSummary[]>('/conversations');
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
