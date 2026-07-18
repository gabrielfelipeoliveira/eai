package com.eai.api.tenant;

import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.TenantStatus;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static CompanyResponse fromDomain(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getStatus(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
