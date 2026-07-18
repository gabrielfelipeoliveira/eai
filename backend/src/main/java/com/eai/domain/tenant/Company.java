package com.eai.domain.tenant;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Company {

    private final UUID id;
    private String name;
    private TenantStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Company(
            UUID id,
            String name,
            TenantStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireText(name, "name");
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Company create(String name) {
        Instant now = Instant.now();
        return new Company(UUID.randomUUID(), name, TenantStatus.ACTIVE, now, now);
    }

    public void update(String name, TenantStatus status) {
        this.name = requireText(name, "name");
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

}
