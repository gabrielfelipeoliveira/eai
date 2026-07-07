package com.eai.infrastructure.persistence.distribution;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "lead_sla_policy")
public class LeadSlaPolicyJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "minutes_to_assign", nullable = false)
    private int minutesToAssign;

    @Column(name = "minutes_to_first_contact", nullable = false)
    private int minutesToFirstContact;

    @Column(nullable = false)
    private boolean active;

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

    public int getMinutesToAssign() {
        return minutesToAssign;
    }

    public void setMinutesToAssign(int minutesToAssign) {
        this.minutesToAssign = minutesToAssign;
    }

    public int getMinutesToFirstContact() {
        return minutesToFirstContact;
    }

    public void setMinutesToFirstContact(int minutesToFirstContact) {
        this.minutesToFirstContact = minutesToFirstContact;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
