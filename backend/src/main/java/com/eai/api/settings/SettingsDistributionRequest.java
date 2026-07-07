package com.eai.api.settings;

import com.eai.domain.distribution.LeadDistributionMode;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SettingsDistributionRequest(
        UUID companyId,
        UUID storeId,
        @NotNull LeadDistributionMode mode,
        boolean active
) {
}
