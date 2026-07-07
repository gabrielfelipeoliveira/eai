package com.eai.domain.lead;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class LeadHistory {

    private final UUID id;
    private final UUID leadId;
    private final UUID userId;
    private final LeadStatus previousStatus;
    private final LeadStatus newStatus;
    private final String description;
    private final Instant createdAt;

    public LeadHistory(UUID id, UUID leadId, UUID userId, LeadStatus previousStatus, LeadStatus newStatus, String description, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.leadId = Objects.requireNonNull(leadId);
        this.userId = userId;
        this.previousStatus = previousStatus;
        this.newStatus = Objects.requireNonNull(newStatus);
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static LeadHistory create(UUID leadId, UUID userId, LeadStatus previousStatus, LeadStatus newStatus, String description) {
        return new LeadHistory(UUID.randomUUID(), leadId, userId, previousStatus, newStatus, description, Instant.now());
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

    public LeadStatus getPreviousStatus() {
        return previousStatus;
    }

    public LeadStatus getNewStatus() {
        return newStatus;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
