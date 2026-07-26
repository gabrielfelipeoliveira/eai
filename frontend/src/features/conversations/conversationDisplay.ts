import type { ConversationMessage, ConversationSummary } from '../../types/message';

export function formatPhone(phone: string) {
  const digits = phone.replace(/\D/g, '');
  if (digits.length === 13 && digits.startsWith('55')) {
    return `+55 (${digits.slice(2, 4)}) ${digits.slice(4, 9)}-${digits.slice(9)}`;
  }
  if (digits.length === 11) {
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
  }
  return phone;
}

export function displayName(conversation: ConversationSummary) {
  return conversation.leadName ?? conversation.contactDisplayName ?? formatPhone(conversation.phone);
}

export function formatInteraction(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatTime(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

export function messageText(message: ConversationMessage) {
  if (message.content) {
    return message.content;
  }
  if (message.type === 'IMAGE') {
    return 'Imagem';
  }
  if (message.type === 'AUDIO') {
    return 'Audio';
  }
  if (message.type === 'DOCUMENT') {
    return 'Documento';
  }
  if (message.type === 'TEMPLATE') {
    return 'Template enviado';
  }
  return 'Mensagem sem texto';
}

export function formatFileSize(sizeBytes: number | null) {
  if (!sizeBytes) {
    return '';
  }
  if (sizeBytes < 1024 * 1024) {
    return `${Math.ceil(sizeBytes / 1024)} KB`;
  }
  return `${(sizeBytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function hasStoredMedia(message: ConversationMessage) {
  return Boolean(message.mediaStorageProvider && message.mediaStorageKey);
}

export function lastMessage(conversation: ConversationSummary) {
  if (conversation.lastMessageContent) {
    return conversation.lastMessageContent;
  }
  if (conversation.lastMessageType === 'IMAGE') {
    return 'Imagem';
  }
  if (conversation.lastMessageType === 'AUDIO') {
    return 'Audio';
  }
  if (conversation.lastMessageType === 'DOCUMENT') {
    return 'Documento';
  }
  if (conversation.lastMessageType === 'TEMPLATE') {
    return 'Template enviado';
  }
  return 'Sem mensagens registradas';
}
