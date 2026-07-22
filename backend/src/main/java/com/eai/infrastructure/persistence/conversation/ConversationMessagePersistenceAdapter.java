package com.eai.infrastructure.persistence.conversation;

import com.eai.application.conversation.ConversationMessageRepository;
import com.eai.domain.conversation.ConversationMessage;
import com.eai.domain.conversation.ConversationMessageDirection;
import com.eai.domain.conversation.ConversationMessageStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
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
    public Optional<ConversationMessage> findByExternalMessageId(String externalMessageId) {
        return externalMessageId == null ? Optional.empty() : repository.findByExternalMessageId(externalMessageId).map(this::toDomain);
    }

    @Override
    public List<ConversationMessage> findByConversationId(UUID conversationId) {
        return repository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ConversationMessage> findLatestByConversationId(UUID conversationId) {
        return repository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId).map(this::toDomain);
    }

    @Override
    public Optional<ConversationMessage> findLatestByConversationIdAndDirection(UUID conversationId, ConversationMessageDirection direction) {
        return repository.findFirstByConversationIdAndDirectionOrderByCreatedAtDesc(conversationId, direction).map(this::toDomain);
    }

    @Override
    public long countByConversationIdAndDirectionAndStatus(UUID conversationId, ConversationMessageDirection direction, ConversationMessageStatus status) {
        return repository.countByConversationIdAndDirectionAndStatus(conversationId, direction, status);
    }

    @Override
    public void markInboundReceivedAsRead(UUID conversationId) {
        repository.markInboundReceivedAsRead(
                conversationId,
                ConversationMessageDirection.INBOUND,
                ConversationMessageStatus.RECEIVED,
                ConversationMessageStatus.READ,
                Instant.now()
        );
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
                entity.getMediaStorageProvider(),
                entity.getMediaStorageKey(),
                entity.getMediaFileName(),
                entity.getMediaSizeBytes(),
                entity.getMediaSha256(),
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
        entity.setMediaStorageProvider(message.getMediaStorageProvider());
        entity.setMediaStorageKey(message.getMediaStorageKey());
        entity.setMediaFileName(message.getMediaFileName());
        entity.setMediaSizeBytes(message.getMediaSizeBytes());
        entity.setMediaSha256(message.getMediaSha256());
        entity.setRawPayload(message.getRawPayload());
        entity.setCreatedAt(message.getCreatedAt());
        entity.setUpdatedAt(message.getUpdatedAt());
        return entity;
    }
}
