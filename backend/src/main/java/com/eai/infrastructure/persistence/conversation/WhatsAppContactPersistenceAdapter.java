package com.eai.infrastructure.persistence.conversation;

import com.eai.application.conversation.WhatsAppContactRepository;
import com.eai.domain.conversation.WhatsAppContact;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class WhatsAppContactPersistenceAdapter implements WhatsAppContactRepository {

    private final SpringDataWhatsAppContactRepository repository;

    public WhatsAppContactPersistenceAdapter(SpringDataWhatsAppContactRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<WhatsAppContact> findByStoreIdAndPhone(UUID storeId, String phone) {
        return repository.findByStoreIdAndPhone(storeId, phone).map(this::toDomain);
    }

    @Override
    public WhatsAppContact save(WhatsAppContact contact) {
        return toDomain(repository.save(toEntity(contact)));
    }

    private WhatsAppContact toDomain(WhatsAppContactJpaEntity entity) {
        return new WhatsAppContact(
                entity.getId(),
                entity.getCompanyId(),
                entity.getStoreId(),
                entity.getLeadId(),
                entity.getPhone(),
                entity.getDisplayName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private WhatsAppContactJpaEntity toEntity(WhatsAppContact contact) {
        WhatsAppContactJpaEntity entity = new WhatsAppContactJpaEntity();
        entity.setId(contact.getId());
        entity.setCompanyId(contact.getCompanyId());
        entity.setStoreId(contact.getStoreId());
        entity.setLeadId(contact.getLeadId());
        entity.setPhone(contact.getPhone());
        entity.setDisplayName(contact.getDisplayName());
        entity.setCreatedAt(contact.getCreatedAt());
        entity.setUpdatedAt(contact.getUpdatedAt());
        return entity;
    }
}
