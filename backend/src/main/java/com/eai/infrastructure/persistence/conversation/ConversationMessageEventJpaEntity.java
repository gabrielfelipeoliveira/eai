package com.eai.infrastructure.persistence.conversation;

import lombok.Getter;
import lombok.Setter;

import com.eai.domain.conversation.ConversationMessageStatus;
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
@Table(name = "conversation_message_events")
public class ConversationMessageEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "external_message_id", nullable = false)
    private String externalMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationMessageStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
