import { describe, expect, it } from 'vitest';
import {
  formatFileSize,
  formatInteraction,
  formatPhone,
  formatTime,
  hasStoredMedia,
  lastMessage,
  messageText,
} from '../features/conversations/conversationDisplay';
import type { ConversationMessage, ConversationSummary } from '../types/message';

function message(overrides: Partial<ConversationMessage>): ConversationMessage {
  return {
    id: 'message-1',
    conversationId: 'conversation-1',
    direction: 'INBOUND',
    type: 'TEXT',
    status: 'RECEIVED',
    externalMessageId: null,
    content: null,
    mediaId: null,
    mediaMimeType: null,
    mediaStorageProvider: null,
    mediaStorageKey: null,
    mediaFileName: null,
    mediaSizeBytes: null,
    mediaSha256: null,
    createdAt: '2026-07-26T12:00:00Z',
    updatedAt: '2026-07-26T12:00:00Z',
    ...overrides,
  };
}

function conversation(overrides: Partial<ConversationSummary>): ConversationSummary {
  return {
    id: 'conversation-1',
    companyId: 'company-1',
    storeId: 'store-1',
    contactId: 'contact-1',
    leadId: null,
    responsibleUserId: null,
    leadName: null,
    phone: '5511999990000',
    contactDisplayName: null,
    lastMessageId: null,
    lastMessageDirection: null,
    lastMessageType: null,
    lastMessageStatus: null,
    lastMessageContent: null,
    lastInteractionAt: '2026-07-26T12:00:00Z',
    unreadCount: 0,
    createdAt: '2026-07-26T12:00:00Z',
    updatedAt: '2026-07-26T12:00:00Z',
    ...overrides,
  };
}

describe('ConversationsPage helpers', () => {
  it('deve formatar telefone brasileiro com e sem codigo do pais', () => {
    expect(formatPhone('5511999990000')).toBe('+55 (11) 99999-0000');
    expect(formatPhone('11999990000')).toBe('(11) 99999-0000');
    expect(formatPhone('+1 555')).toBe('+1 555');
  });

  it('deve formatar datas de interacao e horario de mensagem em PT-BR', () => {
    const value = '2026-07-26T12:34:00Z';

    expect(formatInteraction(value)).toBe(
      new Intl.DateTimeFormat('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      }).format(new Date(value)),
    );
    expect(formatTime(value)).toBe(
      new Intl.DateTimeFormat('pt-BR', {
        hour: '2-digit',
        minute: '2-digit',
      }).format(new Date(value)),
    );
  });

  it('deve exibir texto amigavel para mensagens sem conteudo textual', () => {
    expect(messageText(message({ content: 'Ola' }))).toBe('Ola');
    expect(messageText(message({ type: 'IMAGE' }))).toBe('Imagem');
    expect(messageText(message({ type: 'AUDIO' }))).toBe('Audio');
    expect(messageText(message({ type: 'DOCUMENT' }))).toBe('Documento');
    expect(messageText(message({ type: 'TEMPLATE' }))).toBe('Template enviado');
    expect(messageText(message({ type: 'TEXT' }))).toBe('Mensagem sem texto');
  });

  it('deve formatar tamanho de midia em KB ou MB', () => {
    expect(formatFileSize(null)).toBe('');
    expect(formatFileSize(1)).toBe('1 KB');
    expect(formatFileSize(1536)).toBe('2 KB');
    expect(formatFileSize(1572864)).toBe('1.5 MB');
  });

  it('deve identificar midia armazenada somente quando provider e chave existem', () => {
    expect(hasStoredMedia(message({ mediaStorageProvider: 'local', mediaStorageKey: 'media/key.jpg' }))).toBe(true);
    expect(hasStoredMedia(message({ mediaStorageProvider: 'local', mediaStorageKey: null }))).toBe(false);
    expect(hasStoredMedia(message({ mediaStorageProvider: null, mediaStorageKey: 'media/key.jpg' }))).toBe(false);
  });

  it('deve resolver resumo da ultima mensagem por conteudo ou tipo', () => {
    expect(lastMessage(conversation({ lastMessageContent: 'Cliente respondeu' }))).toBe('Cliente respondeu');
    expect(lastMessage(conversation({ lastMessageType: 'IMAGE' }))).toBe('Imagem');
    expect(lastMessage(conversation({ lastMessageType: 'AUDIO' }))).toBe('Audio');
    expect(lastMessage(conversation({ lastMessageType: 'DOCUMENT' }))).toBe('Documento');
    expect(lastMessage(conversation({ lastMessageType: 'TEMPLATE' }))).toBe('Template enviado');
    expect(lastMessage(conversation({ lastMessageType: null }))).toBe('Sem mensagens registradas');
  });
});
