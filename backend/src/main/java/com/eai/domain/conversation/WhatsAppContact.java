package com.eai.domain.conversation;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class WhatsAppContact {

    private final UUID id;
    private final UUID companyId;
    private final UUID storeId;
    private UUID leadId;
    private final String phone;
    private String displayName;
    private final Instant createdAt;
    private Instant updatedAt;

    public WhatsAppContact(UUID id, UUID companyId, UUID storeId, UUID leadId, String phone, String displayName, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.leadId = leadId;
        this.phone = requirePhone(phone);
        this.displayName = trimToNull(displayName);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static WhatsAppContact create(UUID companyId, UUID storeId, UUID leadId, String phone, String displayName) {
        Instant now = Instant.now();
        return new WhatsAppContact(UUID.randomUUID(), companyId, storeId, leadId, phone, displayName, now, now);
    }

    public void updateLead(UUID leadId) {
        if (leadId != null && !leadId.equals(this.leadId)) {
            this.leadId = leadId;
            this.updatedAt = Instant.now();
        }
    }

    public void updateDisplayName(String displayName) {
        String normalized = trimToNull(displayName);
        if (normalized != null && !normalized.equals(this.displayName)) {
            this.displayName = normalized;
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

    public UUID getLeadId() {
        return leadId;
    }

    public String getPhone() {
        return phone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    private static String requirePhone(String value) {
        String phone = trimToNull(value);
        if (phone == null) {
            throw new IllegalArgumentException("phone is required");
        }
        return phone;
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
