package com.eai.infrastructure.persistence.lead;

import com.eai.application.lead.LeadNoteRepository;
import com.eai.domain.lead.LeadNote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeadNotePersistenceAdapter implements LeadNoteRepository {

    private final SpringDataLeadNoteRepository repository;

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

    @Override
    public java.util.Optional<LeadNote> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private LeadNote toDomain(LeadNoteJpaEntity entity) {
        return new LeadNote(entity.getId(), entity.getLeadId(), entity.getUserId(), entity.getNote(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private LeadNoteJpaEntity toEntity(LeadNote note) {
        LeadNoteJpaEntity entity = new LeadNoteJpaEntity();
        entity.setId(note.getId());
        entity.setLeadId(note.getLeadId());
        entity.setUserId(note.getUserId());
        entity.setNote(note.getNote());
        entity.setCreatedAt(note.getCreatedAt());
        entity.setUpdatedAt(note.getUpdatedAt());
        return entity;
    }
}
