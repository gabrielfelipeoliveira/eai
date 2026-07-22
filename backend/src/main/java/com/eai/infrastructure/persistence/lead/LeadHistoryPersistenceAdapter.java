package com.eai.infrastructure.persistence.lead;

import com.eai.application.lead.LeadHistoryRepository;
import com.eai.domain.lead.LeadHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeadHistoryPersistenceAdapter implements LeadHistoryRepository {

    private final SpringDataLeadHistoryRepository repository;

    @Override
    public List<LeadHistory> findByLeadId(UUID leadId) {
        return repository.findByLeadIdOrderByCreatedAtDesc(leadId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public LeadHistory save(LeadHistory history) {
        return toDomain(repository.save(toEntity(history)));
    }

    private LeadHistory toDomain(LeadHistoryJpaEntity entity) {
        return new LeadHistory(
                entity.getId(),
                entity.getLeadId(),
                entity.getUserId(),
                entity.getPreviousStatus(),
                entity.getNewStatus(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }

    private LeadHistoryJpaEntity toEntity(LeadHistory history) {
        LeadHistoryJpaEntity entity = new LeadHistoryJpaEntity();
        entity.setId(history.getId());
        entity.setLeadId(history.getLeadId());
        entity.setUserId(history.getUserId());
        entity.setPreviousStatus(history.getPreviousStatus());
        entity.setNewStatus(history.getNewStatus());
        entity.setDescription(history.getDescription());
        entity.setCreatedAt(history.getCreatedAt());
        return entity;
    }
}
