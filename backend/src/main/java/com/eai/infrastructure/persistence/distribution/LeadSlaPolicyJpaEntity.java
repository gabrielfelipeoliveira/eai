package com.eai.infrastructure.persistence.distribution;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Getter
@Setter
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

}
