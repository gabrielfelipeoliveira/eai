package com.eai.infrastructure.persistence.lead;

import com.eai.application.lead.LeadTagRepository;
import com.eai.domain.lead.LeadTag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LeadTagPersistenceAdapter implements LeadTagRepository {

    private final SpringDataLeadTagRepository repository;

    public LeadTagPersistenceAdapter(SpringDataLeadTagRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LeadTag> findByLeadId(UUID leadId) {
        return repository.findByLeadIdOrderByNameAsc(leadId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<LeadTag> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public LeadTag save(LeadTag tag) {
        return toDomain(repository.save(toEntity(tag)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private LeadTag toDomain(LeadTagJpaEntity entity) {
        return new LeadTag(entity.getId(), entity.getLeadId(), entity.getName());
    }

    private LeadTagJpaEntity toEntity(LeadTag tag) {
        LeadTagJpaEntity entity = new LeadTagJpaEntity();
        entity.setId(tag.getId());
        entity.setLeadId(tag.getLeadId());
        entity.setName(tag.getName());
        return entity;
    }
}
