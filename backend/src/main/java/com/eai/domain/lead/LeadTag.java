package com.eai.domain.lead;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class LeadTag {

    private final UUID id;
    private final UUID leadId;
    private final String name;

    public LeadTag(UUID id, UUID leadId, String name) {
        this.id = Objects.requireNonNull(id);
        this.leadId = Objects.requireNonNull(leadId);
        this.name = requireText(name);
    }

    public static LeadTag create(UUID leadId, String name) {
        return new LeadTag(UUID.randomUUID(), leadId, name);
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tag name is required");
        }
        return value.trim();
    }
}
