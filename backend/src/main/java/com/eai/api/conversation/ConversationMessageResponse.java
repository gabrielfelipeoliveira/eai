package com.eai.api.conversation;

import com.eai.application.whatsapp.WhatsAppTextSendResult;
import com.eai.application.whatsapp.WhatsAppMediaMessageSendResult;
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
        String mediaStorageProvider,
        String mediaStorageKey,
        String mediaFileName,
        Long mediaSizeBytes,
        String mediaSha256,
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
                message.getMediaStorageProvider(),
                message.getMediaStorageKey(),
                message.getMediaFileName(),
                message.getMediaSizeBytes(),
                message.getMediaSha256(),
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
                null,
                null,
                null,
                null,
                null,
                result.createdAt(),
                result.updatedAt()
        );
    }

    public static ConversationMessageResponse fromMediaSendResult(WhatsAppMediaMessageSendResult result) {
        return new ConversationMessageResponse(
                result.conversationMessageId(),
                result.conversationId(),
                ConversationMessageDirection.OUTBOUND,
                result.type(),
                result.status(),
                result.externalMessageId(),
                result.caption(),
                result.mediaId(),
                result.mediaMimeType(),
                result.mediaStorageProvider(),
                result.mediaStorageKey(),
                result.mediaFileName(),
                result.mediaSizeBytes(),
                result.mediaSha256(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
