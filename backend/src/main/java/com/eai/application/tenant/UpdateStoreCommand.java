package com.eai.application.tenant;

import com.eai.domain.tenant.TenantStatus;

import java.util.UUID;

public record UpdateStoreCommand(
        UUID companyId,
        String name,
        String document,
        String email,
        String phone,
        String city,
        String state,
        String address,
        TenantStatus status
) {
}
