package com.eai.api.message;

import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.message.MessageTemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MessageTemplateRequest(
        @NotNull UUID companyId,
        UUID storeId,
        @NotBlank @Size(max = 120) String name,
        @NotNull MessageTemplateType type,
        @NotBlank String content,
        @Size(max = 20) String languageCode,
        MessageTemplateMetaStatus metaStatus,
        boolean active
) {
}
