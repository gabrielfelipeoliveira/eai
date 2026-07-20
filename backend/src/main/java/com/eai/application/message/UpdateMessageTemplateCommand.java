package com.eai.application.message;

import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.message.MessageTemplateType;

import java.util.UUID;

public record UpdateMessageTemplateCommand(
        UUID companyId,
        UUID storeId,
        String name,
        MessageTemplateType type,
        String content,
        String languageCode,
        MessageTemplateMetaStatus metaStatus,
        boolean active
) {
}
