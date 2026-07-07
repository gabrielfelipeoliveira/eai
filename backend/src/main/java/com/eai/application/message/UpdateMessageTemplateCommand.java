package com.eai.application.message;

import com.eai.domain.message.MessageTemplateType;

import java.util.UUID;

public record UpdateMessageTemplateCommand(
        UUID companyId,
        UUID storeId,
        String name,
        MessageTemplateType type,
        String content,
        boolean active
) {
}
