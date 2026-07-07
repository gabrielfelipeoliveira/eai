package com.eai.api.tenant;

import com.eai.domain.tenant.TenantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 40) String document,
        @Email @Size(max = 180) String email,
        @Size(max = 40) String phone,
        @NotNull TenantStatus status
) {
}
