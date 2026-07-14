package com.eai.domain.conversation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Conversation {

    private final UUID id;
    private final UUID companyId;
    private final UUID storeId;
    private final UUID contactId;
    private UUID leadId;
    private UUID responsibleUserId;
    private final Instant createdAt;
    private Instant updatedAt;

    public Conversation(UUID id, UUID companyId, UUID storeId, UUID contactId, UUID leadId, UUID responsibleUserId, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.contactId = Objects.requireNonNull(contactId);
        this.leadId = leadId;
        this.responsibleUserId = responsibleUserId;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Conversation create(UUID companyId, UUID storeId, UUID contactId, UUID leadId, UUID responsibleUserId) {
        Instant now = Instant.now();
        return new Conversation(UUID.randomUUID(), companyId, storeId, contactId, leadId, responsibleUserId, now, now);
    }

    public void linkLead(UUID leadId, UUID responsibleUserId) {
        boolean changed = false;
        if (leadId != null && !leadId.equals(this.leadId)) {
            this.leadId = leadId;
            changed = true;
        }
        if (responsibleUserId != null && !responsibleUserId.equals(this.responsibleUserId)) {
            this.responsibleUserId = responsibleUserId;
            changed = true;
        }
        if (changed) {
            this.updatedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public UUID getContactId() {
        return contactId;
    }

    public UUID getLeadId() {
        return leadId;
    }

    public UUID getResponsibleUserId() {
        return responsibleUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
