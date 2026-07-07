package com.eai.infrastructure.persistence.distribution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataLeadDistributionConfigRepository extends JpaRepository<LeadDistributionConfigJpaEntity, UUID> {

    Optional<LeadDistributionConfigJpaEntity> findByCompanyIdAndStoreId(UUID companyId, UUID storeId);
}
