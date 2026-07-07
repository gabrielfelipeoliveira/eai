package com.eai.domain.lead;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class LeadNote {

    private final UUID id;
    private final UUID leadId;
    private final UUID userId;
    private final String note;
    private final Instant createdAt;

    public LeadNote(UUID id, UUID leadId, UUID userId, String note, Instant createdAt) {
        this.id = Objects.requireNonNull(id);
        this.leadId = Objects.requireNonNull(leadId);
        this.userId = Objects.requireNonNull(userId);
        this.note = requireText(note);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static LeadNote create(UUID leadId, UUID userId, String note) {
        return new LeadNote(UUID.randomUUID(), leadId, userId, note, Instant.now());
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

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("note is required");
        }
        return value.trim();
    }
}
