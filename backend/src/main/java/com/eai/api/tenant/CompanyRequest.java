package com.eai.api.tenant;

import com.eai.domain.tenant.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 160) String name,
        @NotNull TenantStatus status
) {
}
