package com.eai.infrastructure.persistence.conversation;

import com.eai.application.conversation.ConversationRepository;
import com.eai.domain.conversation.Conversation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConversationPersistenceAdapter implements ConversationRepository {

    private final SpringDataConversationRepository repository;

    public ConversationPersistenceAdapter(SpringDataConversationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Conversation> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findByContactId(UUID contactId) {
        return repository.findByContactId(contactId).map(this::toDomain);
    }

    @Override
    public Optional<Conversation> findByLeadId(UUID leadId) {
        return repository.findByLeadId(leadId).map(this::toDomain);
    }

    @Override
    public List<Conversation> findAll() {
        return repository.findAllByOrderByUpdatedAtDesc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Conversation> findByCompanyId(UUID companyId) {
        return repository.findByCompanyIdOrderByUpdatedAtDesc(companyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Conversation> findByStoreId(UUID storeId) {
        return repository.findByStoreIdOrderByUpdatedAtDesc(storeId).stream().map(this::toDomain).toList();
    }

    @Override
    public Conversation save(Conversation conversation) {
        return toDomain(repository.save(toEntity(conversation)));
    }

    private Conversation toDomain(ConversationJpaEntity entity) {
        return new Conversation(
                entity.getId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getContactId(),
                entity.getLeadId(),
                entity.getResponsibleUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ConversationJpaEntity toEntity(Conversation conversation) {
        ConversationJpaEntity entity = new ConversationJpaEntity();
        entity.setId(conversation.getId());
        entity.setCompanyId(conversation.getCompanyId());
        entity.setStoreId(conversation.getStoreId());
        entity.setContactId(conversation.getContactId());
        entity.setLeadId(conversation.getLeadId());
        entity.setResponsibleUserId(conversation.getResponsibleUserId());
        entity.setCreatedAt(conversation.getCreatedAt());
        entity.setUpdatedAt(conversation.getUpdatedAt());
        return entity;
    }
}
