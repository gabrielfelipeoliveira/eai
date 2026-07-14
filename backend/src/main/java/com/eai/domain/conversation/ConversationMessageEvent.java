package com.eai.domain.conversation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ConversationMessageEvent {

    private final UUID id;
    private final UUID messageId;
    private final String externalMessageId;
    private final ConversationMessageStatus status;
    private final String failureReason;
    private final String rawPayload;
    private final Instant occurredAt;
    private final Instant createdAt;

    public ConversationMessageEvent(
            UUID id,
            UUID messageId,
            String externalMessageId,
            ConversationMessageStatus status,
            String failureReason,
            String rawPayload,
            Instant occurredAt,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.messageId = messageId;
        this.externalMessageId = trimToNull(externalMessageId);
        this.status = Objects.requireNonNull(status);
        this.failureReason = trimToNull(failureReason);
        this.rawPayload = trimToNull(rawPayload);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static ConversationMessageEvent statusReceived(UUID messageId, String externalMessageId, ConversationMessageStatus status, String failureReason, String rawPayload, Instant occurredAt) {
        Instant now = Instant.now();
        return new ConversationMessageEvent(UUID.randomUUID(), messageId, externalMessageId, status, failureReason, rawPayload, occurredAt == null ? now : occurredAt, now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public String getExternalMessageId() {
        return externalMessageId;
    }

    public ConversationMessageStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
