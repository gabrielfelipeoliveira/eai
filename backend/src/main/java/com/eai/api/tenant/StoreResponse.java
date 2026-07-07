package com.eai.api.tenant;

import com.eai.domain.tenant.Store;
import com.eai.domain.tenant.TenantStatus;

import java.time.Instant;
import java.util.UUID;

public record StoreResponse(
        UUID id,
        UUID companyId,
        String name,
        String document,
        String email,
        String phone,
        String city,
        String state,
        String address,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static StoreResponse fromDomain(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getCompanyId(),
                store.getName(),
                store.getDocument(),
                store.getEmail(),
                store.getPhone(),
                store.getCity(),
                store.getState(),
                store.getAddress(),
                store.getStatus(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }
}
