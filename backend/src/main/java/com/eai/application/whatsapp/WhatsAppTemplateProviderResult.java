package com.eai.application.whatsapp;

public record WhatsAppTemplateProviderResult(
        boolean successful,
        int statusCode,
        String externalMessageId,
        String rawResponse
) {
}
