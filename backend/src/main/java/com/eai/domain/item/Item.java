package com.eai.domain.item;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Item {

    private final UUID id;
    private UUID ownerUserId;
    private String name;
    private Vehicle vehicle;
    private final Instant createdAt;
    private Instant updatedAt;

    public Item(UUID id, UUID ownerUserId, String name, Instant createdAt, Instant updatedAt) {
        this(id, ownerUserId, name, null, createdAt, updatedAt);
    }

    public Item(UUID id, UUID ownerUserId, String name, Vehicle vehicle, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.ownerUserId = Objects.requireNonNull(ownerUserId);
        this.name = trimToNull(name);
        this.vehicle = vehicle;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Item create(UUID ownerUserId, String name) {
        Instant now = Instant.now();
        return new Item(UUID.randomUUID(), ownerUserId, name, now, now);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
