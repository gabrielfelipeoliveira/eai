package com.eai.application.conversation;

import com.eai.domain.conversation.ConversationMessageType;

public record IncomingWhatsAppMessage(
        String phone,
        String contactName,
        ConversationMessageType type,
        String externalMessageId,
        String content,
        String mediaId,
        String mediaMimeType,
        String rawPayload
) {
}
