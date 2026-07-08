package com.eai.domain.conversation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ConversationMessage {

    private final UUID id;
    private final UUID conversationId;
    private final ConversationMessageDirection direction;
    private final ConversationMessageType type;
    private ConversationMessageStatus status;
    private final String externalMessageId;
    private final String content;
    private final String mediaId;
    private final String mediaMimeType;
    private final String rawPayload;
    private final Instant createdAt;
    private Instant updatedAt;

    public ConversationMessage(
            UUID id,
            UUID conversationId,
            ConversationMessageDirection direction,
            ConversationMessageType type,
            ConversationMessageStatus status,
            String externalMessageId,
            String content,
            String mediaId,
            String mediaMimeType,
            String rawPayload,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.conversationId = Objects.requireNonNull(conversationId);
        this.direction = Objects.requireNonNull(direction);
        this.type = Objects.requireNonNull(type);
        this.status = Objects.requireNonNull(status);
        this.externalMessageId = trimToNull(externalMessageId);
        this.content = trimToNull(content);
        this.mediaId = trimToNull(mediaId);
        this.mediaMimeType = trimToNull(mediaMimeType);
        this.rawPayload = trimToNull(rawPayload);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static ConversationMessage inbound(UUID conversationId, ConversationMessageType type, String externalMessageId, String content, String mediaId, String mediaMimeType, String rawPayload) {
        Instant now = Instant.now();
        return new ConversationMessage(UUID.randomUUID(), conversationId, ConversationMessageDirection.INBOUND, type, ConversationMessageStatus.RECEIVED, externalMessageId, content, mediaId, mediaMimeType, rawPayload, now, now);
    }

    public static ConversationMessage outbound(UUID conversationId, ConversationMessageType type, ConversationMessageStatus status, String externalMessageId, String content, String rawPayload) {
        Instant now = Instant.now();
        return new ConversationMessage(UUID.randomUUID(), conversationId, ConversationMessageDirection.OUTBOUND, type, status, externalMessageId, content, null, null, rawPayload, now, now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public ConversationMessageDirection getDirection() {
        return direction;
    }

    public ConversationMessageType getType() {
        return type;
    }

    public ConversationMessageStatus getStatus() {
        return status;
    }

    public String getExternalMessageId() {
        return externalMessageId;
    }

    public String getContent() {
        return content;
    }

    public String getMediaId() {
        return mediaId;
    }

    public String getMediaMimeType() {
        return mediaMimeType;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
