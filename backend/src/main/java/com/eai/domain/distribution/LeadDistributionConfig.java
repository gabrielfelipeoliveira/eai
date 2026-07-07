package com.eai.domain.distribution;

import java.util.Objects;
import java.util.UUID;

public class LeadDistributionConfig {

    private final UUID id;
    private final UUID companyId;
    private final UUID storeId;
    private LeadDistributionMode mode;
    private boolean active;

    public LeadDistributionConfig(UUID id, UUID companyId, UUID storeId, LeadDistributionMode mode, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.storeId = Objects.requireNonNull(storeId);
        this.mode = Objects.requireNonNull(mode);
        this.active = active;
    }

    public static LeadDistributionConfig create(UUID companyId, UUID storeId, LeadDistributionMode mode, boolean active) {
        return new LeadDistributionConfig(UUID.randomUUID(), companyId, storeId, mode, active);
    }

    public void update(LeadDistributionMode mode, boolean active) {
        this.mode = Objects.requireNonNull(mode);
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

    public LeadDistributionMode getMode() {
        return mode;
    }

    public boolean isActive() {
        return active;
    }
}
