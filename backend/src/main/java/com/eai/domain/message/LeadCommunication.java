package com.eai.domain.message;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class LeadCommunication {

    private final UUID id;
    private final UUID leadId;
    private final UUID userId;
    private final LeadCommunicationChannel channel;
    private final UUID templateId;
    private final String message;
    private final Instant createdAt;

    public LeadCommunication(UUID id, UUID leadId, UUID userId, LeadCommunicationChannel channel, UUID templateId, String message, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.leadId = Objects.requireNonNull(leadId);
        this.userId = Objects.requireNonNull(userId);
        this.channel = Objects.requireNonNull(channel);
        this.templateId = Objects.requireNonNull(templateId);
        this.message = requireText(message, "message");
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static LeadCommunication create(UUID leadId, UUID userId, LeadCommunicationChannel channel, UUID templateId, String message) {
        return new LeadCommunication(UUID.randomUUID(), leadId, userId, channel, templateId, message, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeadId() {
        return leadId;
    }

    public UUID getUserId() {
        return userId;
    }

    public LeadCommunicationChannel getChannel() {
        return channel;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
