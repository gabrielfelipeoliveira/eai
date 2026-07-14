package com.eai.infrastructure.persistence.conversation;

import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import com.eai.domain.conversation.ConversationMessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_messages")
public class ConversationMessageJpaEntity {

    @Id
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationMessageDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationMessageType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationMessageStatus status;

    @Column(name = "external_message_id")
    private String externalMessageId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_id")
    private String mediaId;

    @Column(name = "media_mime_type")
    private String mediaMimeType;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public ConversationMessageDirection getDirection() {
        return direction;
    }

    public void setDirection(ConversationMessageDirection direction) {
        this.direction = direction;
    }

    public ConversationMessageType getType() {
        return type;
    }

    public void setType(ConversationMessageType type) {
        this.type = type;
    }

    public ConversationMessageStatus getStatus() {
        return status;
    }

    public void setStatus(ConversationMessageStatus status) {
        this.status = status;
    }

    public String getExternalMessageId() {
        return externalMessageId;
    }

    public void setExternalMessageId(String externalMessageId) {
        this.externalMessageId = externalMessageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaMimeType() {
        return mediaMimeType;
    }

    public void setMediaMimeType(String mediaMimeType) {
        this.mediaMimeType = mediaMimeType;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
