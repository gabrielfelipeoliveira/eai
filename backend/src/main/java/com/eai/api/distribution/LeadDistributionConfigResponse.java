package com.eai.api.distribution;

import com.eai.application.distribution.LeadDistributionSettings;
import com.eai.domain.distribution.LeadDistributionMode;

import java.util.UUID;

public record LeadDistributionConfigResponse(
        UUID id,
        UUID companyId,
        UUID storeId,
        LeadDistributionMode mode,
        boolean active,
        UUID slaPolicyId,
        int minutesToAssign,
        int minutesToFirstContact,
        boolean slaActive
) {

    public static LeadDistributionConfigResponse fromSettings(LeadDistributionSettings settings) {
        return new LeadDistributionConfigResponse(
                settings.distributionConfig().getId(),
                settings.distributionConfig().getCompanyId(),
                settings.distributionConfig().getStoreId(),
                settings.distributionConfig().getMode(),
                settings.distributionConfig().isActive(),
                settings.slaPolicy().getId(),
                settings.slaPolicy().getMinutesToAssign(),
                settings.slaPolicy().getMinutesToFirstContact(),
                settings.slaPolicy().isActive()
        );
    }
}
