package com.eai.infrastructure.persistence.message;

import com.eai.application.message.MessageTemplateRepository;
import com.eai.domain.message.MessageTemplate;
import com.eai.domain.message.MessageTemplateMetaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageTemplatePersistenceAdapter implements MessageTemplateRepository {

    private final SpringDataMessageTemplateRepository repository;

    @Override
    public List<MessageTemplate> findAll() {
        return repository.findByDeletedAtIsNullOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findByCompanyId(UUID companyId) {
        return repository.findByCompanyIdAndDeletedAtIsNullOrderByNameAsc(companyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findByStoreScope(UUID companyId, UUID storeId) {
        return repository.findByStoreScopeOrderByNameAsc(companyId, storeId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findActive() {
        return repository.findByActiveTrueAndMetaStatusAndDeletedAtIsNullOrderByNameAsc(MessageTemplateMetaStatus.APPROVED).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findActiveByCompanyId(UUID companyId) {
        return repository.findByCompanyIdAndActiveTrueAndMetaStatusAndDeletedAtIsNullOrderByNameAsc(companyId, MessageTemplateMetaStatus.APPROVED).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findActiveByStoreScope(UUID companyId, UUID storeId) {
        return repository.findActiveByStoreScopeOrderByNameAsc(companyId, storeId, MessageTemplateMetaStatus.APPROVED).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<MessageTemplate> findById(UUID id) {
        return repository.findById(id).filter(entity -> entity.getDeletedAt() == null).map(this::toDomain);
    }

    @Override
    public MessageTemplate save(MessageTemplate template) {
        return toDomain(repository.save(toEntity(template)));
    }

    @Override
    public void softDelete(MessageTemplate template) {
        repository.save(toEntity(template));
    }

    private MessageTemplate toDomain(MessageTemplateJpaEntity entity) {
        return new MessageTemplate(
                entity.getId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getName(),
                entity.getType(),
                entity.getContent(),
                entity.getLanguageCode(),
                entity.getMetaStatus(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private MessageTemplateJpaEntity toEntity(MessageTemplate template) {
        MessageTemplateJpaEntity entity = new MessageTemplateJpaEntity();
        entity.setId(template.getId());
        entity.setCompanyId(template.getCompanyId());
        entity.setStoreId(template.getStoreId());
        entity.setName(template.getName());
        entity.setType(template.getType());
        entity.setContent(template.getContent());
        entity.setLanguageCode(template.getLanguageCode());
        entity.setMetaStatus(template.getMetaStatus());
        entity.setActive(template.isActive());
        entity.setCreatedAt(template.getCreatedAt());
        entity.setUpdatedAt(template.getUpdatedAt());
        entity.setDeletedAt(template.getDeletedAt());
        return entity;
    }
}
