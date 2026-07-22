package com.eai.application.whatsapp;

import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;

import java.time.Instant;
import java.util.UUID;

public record WhatsAppMediaMessageSendResult(
        UUID conversationId,
        UUID conversationMessageId,
        ConversationMessageType type,
        ConversationMessageStatus status,
        String externalMessageId,
        String mediaId,
        String mediaMimeType,
        String mediaStorageProvider,
        String mediaStorageKey,
        String mediaFileName,
        Long mediaSizeBytes,
        String mediaSha256,
        String caption,
        String providerResponse,
        Instant createdAt,
        Instant updatedAt
) {
}
