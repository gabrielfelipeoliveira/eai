package com.eai.application.whatsapp;

import com.eai.domain.conversation.ConversationMessageStatus;

public record WhatsAppMessageStatusEvent(
        String externalMessageId,
        ConversationMessageStatus status,
        String rawPayload
) {
}
