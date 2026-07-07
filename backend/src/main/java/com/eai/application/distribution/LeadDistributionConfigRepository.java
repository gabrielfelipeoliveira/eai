package com.eai.application.distribution;

import com.eai.domain.distribution.LeadDistributionConfig;

import java.util.Optional;
import java.util.UUID;

public interface LeadDistributionConfigRepository {

    Optional<LeadDistributionConfig> findByCompanyIdAndStoreId(UUID companyId, UUID storeId);

    LeadDistributionConfig save(LeadDistributionConfig config);
}
