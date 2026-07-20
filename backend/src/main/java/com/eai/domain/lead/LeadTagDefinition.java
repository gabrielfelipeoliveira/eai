package com.eai.domain.lead;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class LeadTagDefinition {

    private final UUID id;
    private final String name;
    private final String type;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;

    public LeadTagDefinition(UUID id, String name, String type, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.name = requireText(name, "tag name is required");
        this.type = requireText(type, "tag type is required").toUpperCase();
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static LeadTagDefinition create(String name, String type) {
        Instant now = Instant.now();
        return new LeadTagDefinition(UUID.randomUUID(), name, type, true, now, now);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
