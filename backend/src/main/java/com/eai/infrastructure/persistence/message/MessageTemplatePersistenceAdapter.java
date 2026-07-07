package com.eai.infrastructure.persistence.message;

import com.eai.application.message.MessageTemplateRepository;
import com.eai.domain.message.MessageTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MessageTemplatePersistenceAdapter implements MessageTemplateRepository {

    private final SpringDataMessageTemplateRepository repository;

    public MessageTemplatePersistenceAdapter(SpringDataMessageTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<MessageTemplate> findAll() {
        return repository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findByCompanyId(UUID companyId) {
        return repository.findByCompanyIdOrderByNameAsc(companyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findByStoreId(UUID storeId) {
        return repository.findByStoreIdOrderByNameAsc(storeId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findActiveByCompanyId(UUID companyId) {
        return repository.findByCompanyIdAndActiveTrueOrderByNameAsc(companyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MessageTemplate> findActiveByStoreId(UUID storeId) {
        return repository.findByStoreIdAndActiveTrueOrderByNameAsc(storeId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<MessageTemplate> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public MessageTemplate save(MessageTemplate template) {
        return toDomain(repository.save(toEntity(template)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private MessageTemplate toDomain(MessageTemplateJpaEntity entity) {
        return new MessageTemplate(
                entity.getId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getName(),
                entity.getType(),
                entity.getContent(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
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
        entity.setActive(template.isActive());
        entity.setCreatedAt(template.getCreatedAt());
        entity.setUpdatedAt(template.getUpdatedAt());
        return entity;
    }
}
