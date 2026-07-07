package com.eai.infrastructure.persistence.distribution;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataLeadSlaPolicyRepository extends JpaRepository<LeadSlaPolicyJpaEntity, UUID> {

    Optional<LeadSlaPolicyJpaEntity> findByCompanyIdAndStoreId(UUID companyId, UUID storeId);
}
