package com.eai.application.whatsapp;

import com.eai.domain.conversation.ConversationMessageStatus;

import java.time.Instant;
import java.util.UUID;

public record WhatsAppTextSendResult(
        UUID conversationId,
        UUID conversationMessageId,
        ConversationMessageStatus status,
        String externalMessageId,
        String message,
        String providerResponse,
        Instant createdAt,
        Instant updatedAt
) {
}
