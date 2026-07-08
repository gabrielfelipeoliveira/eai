package com.eai.infrastructure.persistence.conversation;

import com.eai.application.conversation.ConversationMessageRepository;
import com.eai.domain.conversation.ConversationMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConversationMessagePersistenceAdapter implements ConversationMessageRepository {

    private final SpringDataConversationMessageRepository repository;

    public ConversationMessagePersistenceAdapter(SpringDataConversationMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByExternalMessageId(String externalMessageId) {
        return externalMessageId != null && repository.existsByExternalMessageId(externalMessageId);
    }

    @Override
    public Optional<ConversationMessage> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ConversationMessage> findByConversationId(UUID conversationId) {
        return repository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream().map(this::toDomain).toList();
    }

    @Override
    public ConversationMessage save(ConversationMessage message) {
        return toDomain(repository.save(toEntity(message)));
    }

    private ConversationMessage toDomain(ConversationMessageJpaEntity entity) {
        return new ConversationMessage(
                entity.getId(),
                entity.getConversationId(),
                entity.getDirection(),
                entity.getType(),
                entity.getStatus(),
                entity.getExternalMessageId(),
                entity.getContent(),
                entity.getMediaId(),
                entity.getMediaMimeType(),
                entity.getRawPayload(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ConversationMessageJpaEntity toEntity(ConversationMessage message) {
        ConversationMessageJpaEntity entity = new ConversationMessageJpaEntity();
        entity.setId(message.getId());
        entity.setConversationId(message.getConversationId());
        entity.setDirection(message.getDirection());
        entity.setType(message.getType());
        entity.setStatus(message.getStatus());
        entity.setExternalMessageId(message.getExternalMessageId());
        entity.setContent(message.getContent());
        entity.setMediaId(message.getMediaId());
        entity.setMediaMimeType(message.getMediaMimeType());
        entity.setRawPayload(message.getRawPayload());
        entity.setCreatedAt(message.getCreatedAt());
        entity.setUpdatedAt(message.getUpdatedAt());
        return entity;
    }
}
