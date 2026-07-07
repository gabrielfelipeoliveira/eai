package com.eai.domain.distribution;

import java.util.Objects;
import java.util.UUID;

public class LeadSlaPolicy {

    private final UUID id;
    private final UUID companyId;
    private final UUID storeId;
    private int minutesToAssign;
    private int minutesToFirstContact;
    private boolean active;

    public LeadSlaPolicy(UUID id, UUID companyId, UUID storeId, int minutesToAssign, int minutesToFirstContact, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        update(minutesToAssign, minutesToFirstContact, active);
    }

    public static LeadSlaPolicy create(UUID companyId, UUID storeId, int minutesToAssign, int minutesToFirstContact, boolean active) {
        return new LeadSlaPolicy(UUID.randomUUID(), companyId, storeId, minutesToAssign, minutesToFirstContact, active);
    }

    public void update(int minutesToAssign, int minutesToFirstContact, boolean active) {
        if (minutesToAssign <= 0 || minutesToFirstContact <= 0) {
            throw new IllegalArgumentException("SLA minutes must be greater than zero");
        }
        this.minutesToAssign = minutesToAssign;
        this.minutesToFirstContact = minutesToFirstContact;
        this.active = active;
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

    public int getMinutesToAssign() {
        return minutesToAssign;
    }

    public int getMinutesToFirstContact() {
        return minutesToFirstContact;
    }

    public boolean isActive() {
        return active;
    }
}
