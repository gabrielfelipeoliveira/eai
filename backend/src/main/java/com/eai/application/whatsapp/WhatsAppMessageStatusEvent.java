package com.eai.application.whatsapp;

import com.eai.domain.conversation.ConversationMessageStatus;

import java.time.Instant;

public record WhatsAppMessageStatusEvent(
        String externalMessageId,
        ConversationMessageStatus status,
        String failureReason,
        Instant occurredAt,
        String rawPayload
) {
}
