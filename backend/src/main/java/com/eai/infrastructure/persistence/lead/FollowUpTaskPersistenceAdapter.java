package com.eai.infrastructure.persistence.lead;

import com.eai.application.lead.FollowUpTaskRepository;
import com.eai.domain.lead.FollowUpTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FollowUpTaskPersistenceAdapter implements FollowUpTaskRepository {

    private final SpringDataFollowUpTaskRepository repository;

    @Override
    public FollowUpTask save(FollowUpTask task) {
        return toDomain(repository.save(toEntity(task)));
    }

    @Override
    public Optional<FollowUpTask> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<FollowUpTask> findVisible(UUID scopeCompanyId, UUID scopeStoreId, UUID userId) {
        return repository.findVisible(scopeCompanyId, scopeStoreId, userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<FollowUpTask> findByLeadId(UUID leadId) {
        return repository.findByLeadIdOrderByDueAtAsc(leadId).stream()
                .map(this::toDomain)
                .toList();
    }

    private FollowUpTask toDomain(FollowUpTaskJpaEntity entity) {
        return new FollowUpTask(
                entity.getId(),
                entity.getLeadId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDueAt(),
                entity.getCompletedAt(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private FollowUpTaskJpaEntity toEntity(FollowUpTask task) {
        FollowUpTaskJpaEntity entity = new FollowUpTaskJpaEntity();
        entity.setId(task.getId());
        entity.setLeadId(task.getLeadId());
        entity.setUserId(task.getUserId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setDueAt(task.getDueAt());
        entity.setCompletedAt(task.getCompletedAt());
        entity.setStatus(task.getStatus());
        entity.setCreatedAt(task.getCreatedAt());
        entity.setUpdatedAt(task.getUpdatedAt());
        return entity;
    }
}
