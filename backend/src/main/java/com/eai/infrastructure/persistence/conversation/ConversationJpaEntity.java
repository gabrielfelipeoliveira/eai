package com.eai.infrastructure.persistence.conversation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class ConversationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "contact_id", nullable = false)
    private UUID contactId;

    @Column(name = "lead_id")
    private UUID leadId;

    @Column(name = "responsible_user_id")
    private UUID responsibleUserId;

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

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public void setStoreId(UUID storeId) {
        this.storeId = storeId;
    }

    public UUID getContactId() {
        return contactId;
    }

    public void setContactId(UUID contactId) {
        this.contactId = contactId;
    }

    public UUID getLeadId() {
        return leadId;
    }

    public void setLeadId(UUID leadId) {
        this.leadId = leadId;
    }

    public UUID getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(UUID responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
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
