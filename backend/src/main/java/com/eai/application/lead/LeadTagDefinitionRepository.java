package com.eai.application.lead;

import com.eai.domain.lead.LeadTagDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadTagDefinitionRepository {

    List<LeadTagDefinition> findAllActive();

    Optional<LeadTagDefinition> findById(UUID id);

    Optional<LeadTagDefinition> findActiveByName(String name);

    boolean existsByNameIgnoreCase(String name);

    LeadTagDefinition save(LeadTagDefinition tagDefinition);
}
