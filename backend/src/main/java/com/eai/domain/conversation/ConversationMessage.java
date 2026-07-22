package com.eai.domain.conversation;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
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
    private final String mediaStorageProvider;
    private final String mediaStorageKey;
    private final String mediaFileName;
    private final Long mediaSizeBytes;
    private final String mediaSha256;
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
        this(id, conversationId, direction, type, status, externalMessageId, content, mediaId, mediaMimeType, null, null, null, null, null, rawPayload, createdAt, updatedAt);
    }

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
            String mediaStorageProvider,
            String mediaStorageKey,
            String mediaFileName,
            Long mediaSizeBytes,
            String mediaSha256,
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
        this.mediaStorageProvider = trimToNull(mediaStorageProvider);
        this.mediaStorageKey = trimToNull(mediaStorageKey);
        this.mediaFileName = trimToNull(mediaFileName);
        this.mediaSizeBytes = mediaSizeBytes;
        this.mediaSha256 = trimToNull(mediaSha256);
        this.rawPayload = trimToNull(rawPayload);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static ConversationMessage inbound(UUID conversationId, ConversationMessageType type, String externalMessageId, String content, String mediaId, String mediaMimeType, String rawPayload) {
        return inbound(conversationId, type, externalMessageId, content, mediaId, mediaMimeType, null, null, null, null, null, rawPayload);
    }

    public static ConversationMessage inbound(
            UUID conversationId,
            ConversationMessageType type,
            String externalMessageId,
            String content,
            String mediaId,
            String mediaMimeType,
            String mediaStorageProvider,
            String mediaStorageKey,
            String mediaFileName,
            Long mediaSizeBytes,
            String mediaSha256,
            String rawPayload
    ) {
        Instant now = Instant.now();
        return new ConversationMessage(UUID.randomUUID(), conversationId, ConversationMessageDirection.INBOUND, type, ConversationMessageStatus.RECEIVED, externalMessageId, content, mediaId, mediaMimeType, mediaStorageProvider, mediaStorageKey, mediaFileName, mediaSizeBytes, mediaSha256, rawPayload, now, now);
    }

    public static ConversationMessage outbound(UUID conversationId, ConversationMessageType type, ConversationMessageStatus status, String externalMessageId, String content, String rawPayload) {
        return outbound(conversationId, type, status, externalMessageId, content, null, null, null, null, null, null, null, rawPayload);
    }

    public static ConversationMessage outbound(
            UUID conversationId,
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
            String rawPayload
    ) {
        Instant now = Instant.now();
        return new ConversationMessage(UUID.randomUUID(), conversationId, ConversationMessageDirection.OUTBOUND, type, status, externalMessageId, content, mediaId, mediaMimeType, mediaStorageProvider, mediaStorageKey, mediaFileName, mediaSizeBytes, mediaSha256, rawPayload, now, now);
    }

    public void updateStatus(ConversationMessageStatus status) {
        this.status = Objects.requireNonNull(status);
        this.updatedAt = Instant.now();
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
