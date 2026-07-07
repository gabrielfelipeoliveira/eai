package com.eai.domain.lead;

import java.util.Objects;
import java.util.UUID;

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

    public UUID getId() {
        return id;
    }

    public UUID getLeadId() {
        return leadId;
    }

    public String getName() {
        return name;
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tag name is required");
        }
        return value.trim();
    }
}
