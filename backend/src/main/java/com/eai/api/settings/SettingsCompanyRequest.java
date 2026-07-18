package com.eai.api.settings;

import com.eai.domain.tenant.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SettingsCompanyRequest(
        UUID companyId,
        @NotBlank @Size(max = 160) String name,
        @NotNull TenantStatus status
) {
}
