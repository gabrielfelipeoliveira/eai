package com.eai.domain.lead;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class LeadTag {

    private final UUID id;
    private final UUID leadId;
    private final UUID tagId;
    private final String name;
    private final String type;

    public LeadTag(UUID id, UUID leadId, UUID tagId, String name, String type) {
        this.id = Objects.requireNonNull(id);
        this.leadId = Objects.requireNonNull(leadId);
        this.tagId = Objects.requireNonNull(tagId);
        this.name = requireText(name);
        this.type = requireText(type);
    }

    public static LeadTag create(UUID leadId, LeadTagDefinition tagDefinition) {
        return new LeadTag(UUID.randomUUID(), leadId, tagDefinition.getId(), tagDefinition.getName(), tagDefinition.getType());
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tag name is required");
        }
        return value.trim();
    }
}
