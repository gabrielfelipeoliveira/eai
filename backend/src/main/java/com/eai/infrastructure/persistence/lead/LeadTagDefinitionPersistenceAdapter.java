package com.eai.infrastructure.persistence.lead;

import com.eai.application.lead.LeadTagDefinitionRepository;
import com.eai.domain.lead.LeadTagDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeadTagDefinitionPersistenceAdapter implements LeadTagDefinitionRepository {

    private final SpringDataLeadTagDefinitionRepository repository;

    @Override
    public List<LeadTagDefinition> findAllActive() {
        return repository.findByActiveTrueOrderByTypeAscNameAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<LeadTagDefinition> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<LeadTagDefinition> findActiveByName(String name) {
        return repository.findByActiveTrueAndNameIgnoreCase(name).map(this::toDomain);
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    @Override
    public LeadTagDefinition save(LeadTagDefinition tagDefinition) {
        return toDomain(repository.save(toEntity(tagDefinition)));
    }

    private LeadTagDefinition toDomain(LeadTagDefinitionJpaEntity entity) {
        return new LeadTagDefinition(entity.getId(), entity.getName(), entity.getType(), entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private LeadTagDefinitionJpaEntity toEntity(LeadTagDefinition tagDefinition) {
        LeadTagDefinitionJpaEntity entity = new LeadTagDefinitionJpaEntity();
        entity.setId(tagDefinition.getId());
        entity.setName(tagDefinition.getName());
        entity.setType(tagDefinition.getType());
        entity.setActive(tagDefinition.isActive());
        entity.setCreatedAt(tagDefinition.getCreatedAt());
        entity.setUpdatedAt(tagDefinition.getUpdatedAt());
        return entity;
    }
}
