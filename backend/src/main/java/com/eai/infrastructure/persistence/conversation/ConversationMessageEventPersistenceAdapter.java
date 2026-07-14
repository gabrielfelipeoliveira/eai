package com.eai.infrastructure.persistence.conversation;

import com.eai.application.conversation.ConversationMessageEventRepository;
import com.eai.domain.conversation.ConversationMessageEvent;
import org.springframework.stereotype.Component;

@Component
public class ConversationMessageEventPersistenceAdapter implements ConversationMessageEventRepository {

    private final SpringDataConversationMessageEventRepository repository;

    public ConversationMessageEventPersistenceAdapter(SpringDataConversationMessageEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public ConversationMessageEvent save(ConversationMessageEvent event) {
        return toDomain(repository.save(toEntity(event)));
    }

    private ConversationMessageEvent toDomain(ConversationMessageEventJpaEntity entity) {
        return new ConversationMessageEvent(
                entity.getId(),
                entity.getMessageId(),
                entity.getExternalMessageId(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getRawPayload(),
                entity.getOccurredAt(),
                entity.getCreatedAt()
        );
    }

    private ConversationMessageEventJpaEntity toEntity(ConversationMessageEvent event) {
        ConversationMessageEventJpaEntity entity = new ConversationMessageEventJpaEntity();
        entity.setId(event.getId());
        entity.setMessageId(event.getMessageId());
        entity.setExternalMessageId(event.getExternalMessageId());
        entity.setStatus(event.getStatus());
        entity.setFailureReason(event.getFailureReason());
        entity.setRawPayload(event.getRawPayload());
        entity.setOccurredAt(event.getOccurredAt());
        entity.setCreatedAt(event.getCreatedAt());
        return entity;
    }
}
