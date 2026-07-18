package com.eai.infrastructure.persistence.distribution;

import lombok.Getter;
import lombok.Setter;

import com.eai.domain.distribution.LeadDistributionMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lead_distribution_config")
public class LeadDistributionConfigJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadDistributionMode mode;

    @Column(nullable = false)
    private boolean active;

}
