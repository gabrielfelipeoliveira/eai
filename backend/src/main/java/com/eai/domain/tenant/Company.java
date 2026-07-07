package com.eai.domain.tenant;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Company {

    private final UUID id;
    private String name;
    private String document;
    private String email;
    private String phone;
    private TenantStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Company(
            UUID id,
            String name,
            String document,
            String email,
            String phone,
            TenantStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireText(name, "name");
        this.document = requireText(document, "document");
        this.email = normalizeOptional(email);
        this.phone = normalizeOptional(phone);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Company create(String name, String document, String email, String phone) {
        Instant now = Instant.now();
        return new Company(UUID.randomUUID(), name, document, email, phone, TenantStatus.ACTIVE, now, now);
    }

    public void update(String name, String document, String email, String phone, TenantStatus status) {
        this.name = requireText(name, "name");
        this.document = requireText(document, "document");
        this.email = normalizeOptional(email);
        this.phone = normalizeOptional(phone);
        this.status = Objects.requireNonNull(status);
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDocument() {
        return document;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
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
