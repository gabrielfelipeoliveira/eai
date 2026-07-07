package com.eai.api.settings;

import com.eai.domain.tenant.TenantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SettingsCompanyRequest(
        UUID companyId,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 40) String document,
        @Email @Size(max = 180) String email,
        @Size(max = 40) String phone,
        @NotNull TenantStatus status
) {
}
