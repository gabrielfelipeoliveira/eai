package com.eai.domain.tenant;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Store {

    private final UUID id;
    private UUID companyId;
    private String name;
    private String document;
    private String email;
    private String phone;
    private String city;
    private String state;
    private String address;
    private TenantStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Store(
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
        this.id = Objects.requireNonNull(id);
        this.companyId = Objects.requireNonNull(companyId);
        this.name = requireText(name, "name");
        this.document = requireText(document, "document");
        this.email = normalizeOptional(email);
        this.phone = normalizeOptional(phone);
        this.city = normalizeOptional(city);
        this.state = normalizeOptional(state);
        this.address = normalizeOptional(address);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Store create(
            UUID companyId,
            String name,
            String document,
            String email,
            String phone,
            String city,
            String state,
            String address
    ) {
        Instant now = Instant.now();
        return new Store(UUID.randomUUID(), companyId, name, document, email, phone, city, state, address, TenantStatus.ACTIVE, now, now);
    }

    public void update(
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
        this.companyId = Objects.requireNonNull(companyId);
        this.name = requireText(name, "name");
        this.document = requireText(document, "document");
        this.email = normalizeOptional(email);
        this.phone = normalizeOptional(phone);
        this.city = normalizeOptional(city);
        this.state = normalizeOptional(state);
        this.address = normalizeOptional(address);
        this.status = Objects.requireNonNull(status);
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
