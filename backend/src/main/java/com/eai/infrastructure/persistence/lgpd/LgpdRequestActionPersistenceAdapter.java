package com.eai.infrastructure.persistence.lgpd;

import com.eai.application.lgpd.LgpdRequestActionRepository;
import com.eai.domain.lgpd.LgpdRequestAction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LgpdRequestActionPersistenceAdapter implements LgpdRequestActionRepository {

    private final SpringDataLgpdRequestActionRepository repository;

    public LgpdRequestActionPersistenceAdapter(SpringDataLgpdRequestActionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LgpdRequestAction> findByRequestId(UUID requestId) {
        return repository.findByRequestIdOrderByCreatedAtDesc(requestId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public LgpdRequestAction save(LgpdRequestAction action) {
        return toDomain(repository.save(toEntity(action)));
    }

    private LgpdRequestAction toDomain(LgpdRequestActionJpaEntity entity) {
        return new LgpdRequestAction(
                entity.getId(),
                entity.getRequestId(),
                entity.getExecutorUserId(),
                entity.getActionType(),
                entity.getResolution(),
                entity.getFinalStatus(),
                entity.getCreatedAt()
        );
    }

    private LgpdRequestActionJpaEntity toEntity(LgpdRequestAction action) {
        LgpdRequestActionJpaEntity entity = new LgpdRequestActionJpaEntity();
        entity.setId(action.getId());
        entity.setRequestId(action.getRequestId());
        entity.setExecutorUserId(action.getExecutorUserId());
        entity.setActionType(action.getActionType());
        entity.setResolution(action.getResolution());
        entity.setFinalStatus(action.getFinalStatus());
        entity.setCreatedAt(action.getCreatedAt());
        return entity;
    }
}
