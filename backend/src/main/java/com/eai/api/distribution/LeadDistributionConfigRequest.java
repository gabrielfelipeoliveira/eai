package com.eai.api.distribution;

import com.eai.domain.distribution.LeadDistributionMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LeadDistributionConfigRequest(
        UUID companyId,
        UUID storeId,
        @NotNull LeadDistributionMode mode,
        boolean active,
        @Min(1) int minutesToAssign,
        @Min(1) int minutesToFirstContact,
        boolean slaActive
) {
}
