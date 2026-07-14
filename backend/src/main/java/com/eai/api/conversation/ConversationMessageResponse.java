package com.eai.api.conversation;

import com.eai.application.whatsapp.WhatsAppTextSendResult;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;

import java.time.Instant;
import java.util.UUID;

public record ConversationMessageResponse(
        UUID id,
        UUID conversationId,
        ConversationMessageDirection direction,
        ConversationMessageType type,
        ConversationMessageStatus status,
        String externalMessageId,
        String content,
        String mediaId,
        String mediaMimeType,
        Instant createdAt,
        Instant updatedAt
) {

    public static ConversationMessageResponse fromDomain(ConversationMessage message) {
        return new ConversationMessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getDirection(),
                message.getType(),
                message.getStatus(),
                message.getExternalMessageId(),
                message.getContent(),
                message.getMediaId(),
                message.getMediaMimeType(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    public static ConversationMessageResponse fromTextSendResult(WhatsAppTextSendResult result) {
        return new ConversationMessageResponse(
                result.conversationMessageId(),
                result.conversationId(),
                ConversationMessageDirection.OUTBOUND,
                ConversationMessageType.TEXT,
                result.status(),
                result.externalMessageId(),
                result.message(),
                null,
                null,
                result.createdAt(),
                result.updatedAt()
        );
    }
}
