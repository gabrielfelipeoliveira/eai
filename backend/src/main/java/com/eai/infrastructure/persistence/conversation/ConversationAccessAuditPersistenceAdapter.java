package com.eai.infrastructure.persistence.conversation;

import com.eai.application.conversation.ConversationAccessAuditRepository;
import com.eai.domain.conversation.ConversationAccessAudit;
import org.springframework.stereotype.Component;

@Component
public class ConversationAccessAuditPersistenceAdapter implements ConversationAccessAuditRepository {

    private final SpringDataConversationAccessAuditRepository repository;

    public ConversationAccessAuditPersistenceAdapter(SpringDataConversationAccessAuditRepository repository) {
        this.repository = repository;
    }

    @Override
    public ConversationAccessAudit save(ConversationAccessAudit audit) {
        return toDomain(repository.save(toEntity(audit)));
    }

    private ConversationAccessAudit toDomain(ConversationAccessAuditJpaEntity entity) {
        return new ConversationAccessAudit(
                entity.getId(),
                entity.getConversationId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getLeadId(),
                entity.getActorUserId(),
                entity.getActorRole(),
                entity.getAccessType(),
                entity.getAccessedAt()
        );
    }

    private ConversationAccessAuditJpaEntity toEntity(ConversationAccessAudit audit) {
        ConversationAccessAuditJpaEntity entity = new ConversationAccessAuditJpaEntity();
        entity.setId(audit.getId());
        entity.setConversationId(audit.getConversationId());
        entity.setCompanyId(audit.getCompanyId());
        entity.setStoreId(audit.getStoreId());
        entity.setLeadId(audit.getLeadId());
        entity.setActorUserId(audit.getActorUserId());
        entity.setActorRole(audit.getActorRole());
        entity.setAccessType(audit.getAccessType());
        entity.setAccessedAt(audit.getAccessedAt());
        return entity;
    }
}
