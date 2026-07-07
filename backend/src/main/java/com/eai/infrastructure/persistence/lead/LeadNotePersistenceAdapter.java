package com.eai.infrastructure.persistence.lead;

import com.eai.application.lead.LeadNoteRepository;
import com.eai.domain.lead.LeadNote;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LeadNotePersistenceAdapter implements LeadNoteRepository {

    private final SpringDataLeadNoteRepository repository;

    public LeadNotePersistenceAdapter(SpringDataLeadNoteRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LeadNote> findByLeadId(UUID leadId) {
        return repository.findByLeadIdOrderByCreatedAtDesc(leadId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public LeadNote save(LeadNote note) {
        return toDomain(repository.save(toEntity(note)));
    }

    private LeadNote toDomain(LeadNoteJpaEntity entity) {
        return new LeadNote(entity.getId(), entity.getLeadId(), entity.getUserId(), entity.getNote(), entity.getCreatedAt());
    }

    private LeadNoteJpaEntity toEntity(LeadNote note) {
        LeadNoteJpaEntity entity = new LeadNoteJpaEntity();
        entity.setId(note.getId());
        entity.setLeadId(note.getLeadId());
        entity.setUserId(note.getUserId());
        entity.setNote(note.getNote());
        entity.setCreatedAt(note.getCreatedAt());
        return entity;
    }
}
