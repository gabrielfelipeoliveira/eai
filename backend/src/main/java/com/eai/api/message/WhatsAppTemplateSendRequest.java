package com.eai.api.message;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record WhatsAppTemplateSendRequest(
        @NotNull UUID templateId,
        @Size(max = 20) String languageCode
) {
}
