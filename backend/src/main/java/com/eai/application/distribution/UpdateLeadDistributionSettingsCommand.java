package com.eai.application.distribution;

import com.eai.domain.distribution.LeadDistributionMode;

import java.util.UUID;

public record UpdateLeadDistributionSettingsCommand(
        UUID companyId,
        UUID storeId,
        LeadDistributionMode mode,
        boolean distributionActive,
        int minutesToAssign,
        int minutesToFirstContact,
        boolean slaActive
) {
}
