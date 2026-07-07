package com.eai.api.message;

import com.eai.domain.message.MessageTemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MessageTemplateRequest(
        @NotNull UUID companyId,
        @NotNull UUID storeId,
        @NotBlank @Size(max = 120) String name,
        @NotNull MessageTemplateType type,
        @NotBlank String content,
        boolean active
) {
}
