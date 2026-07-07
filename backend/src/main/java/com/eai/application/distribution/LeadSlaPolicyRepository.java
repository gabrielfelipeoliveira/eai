package com.eai.application.distribution;

import com.eai.domain.distribution.LeadSlaPolicy;

import java.util.Optional;
import java.util.UUID;

public interface LeadSlaPolicyRepository {

    Optional<LeadSlaPolicy> findByCompanyIdAndStoreId(UUID companyId, UUID storeId);

    LeadSlaPolicy save(LeadSlaPolicy policy);
}
