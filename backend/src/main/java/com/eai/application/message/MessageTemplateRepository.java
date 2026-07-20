package com.eai.application.message;

import com.eai.domain.message.MessageTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageTemplateRepository {

    List<MessageTemplate> findAll();

    List<MessageTemplate> findByCompanyId(UUID companyId);

    List<MessageTemplate> findByStoreScope(UUID companyId, UUID storeId);

    List<MessageTemplate> findActive();

    List<MessageTemplate> findActiveByCompanyId(UUID companyId);

    List<MessageTemplate> findActiveByStoreScope(UUID companyId, UUID storeId);

    Optional<MessageTemplate> findById(UUID id);

    MessageTemplate save(MessageTemplate template);

    void softDelete(MessageTemplate template);
}
