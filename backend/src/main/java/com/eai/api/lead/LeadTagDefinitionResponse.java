package com.eai.api.lead;

import com.eai.domain.lead.LeadTagDefinition;

import java.time.Instant;
import java.util.UUID;

public record LeadTagDefinitionResponse(UUID id, String name, String type, boolean active, Instant createdAt, Instant updatedAt) {

    public static LeadTagDefinitionResponse fromDomain(LeadTagDefinition tagDefinition) {
        return new LeadTagDefinitionResponse(
                tagDefinition.getId(),
                tagDefinition.getName(),
                tagDefinition.getType(),
                tagDefinition.isActive(),
                tagDefinition.getCreatedAt(),
                tagDefinition.getUpdatedAt()
        );
    }
}
