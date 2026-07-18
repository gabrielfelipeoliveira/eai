package com.eai.infrastructure.persistence.conversation;

import lombok.Getter;
import lombok.Setter;

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

@Getter
@Setter
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

}
