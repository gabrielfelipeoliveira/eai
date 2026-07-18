package com.eai.domain.item;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Vehicle {

    private final UUID id;
    private UUID itemId;
    private String name;
    private Integer year;
    private String model;
    private BigDecimal value;
    private final Instant createdAt;
    private Instant updatedAt;

    public Vehicle(UUID id, UUID itemId, String name, Integer year, String model, BigDecimal value, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id);
        this.itemId = Objects.requireNonNull(itemId);
        this.name = trimToNull(name);
        this.year = year;
        this.model = trimToNull(model);
        this.value = value;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Vehicle create(UUID itemId, String name, Integer year, String model, BigDecimal value) {
        Instant now = Instant.now();
        return new Vehicle(UUID.randomUUID(), itemId, name, year, model, value, now, now);
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
