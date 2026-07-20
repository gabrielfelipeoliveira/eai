package com.eai.api.message;

import com.eai.domain.message.MessageTemplate;
import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.message.MessageTemplateType;

import java.time.Instant;
import java.util.UUID;

public record MessageTemplateResponse(
        UUID id,
        UUID companyId,
        UUID storeId,
        String name,
        MessageTemplateType type,
        String content,
        String languageCode,
        MessageTemplateMetaStatus metaStatus,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    public static MessageTemplateResponse fromDomain(MessageTemplate template) {
        return new MessageTemplateResponse(
                template.getId(),
                template.getCompanyId(),
                template.getStoreId(),
                template.getName(),
                template.getType(),
                template.getContent(),
                template.getLanguageCode(),
                template.getMetaStatus(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt(),
                template.getDeletedAt()
        );
    }
}
