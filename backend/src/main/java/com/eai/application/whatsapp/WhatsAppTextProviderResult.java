package com.eai.application.whatsapp;

public record WhatsAppTextProviderResult(
        boolean successful,
        int statusCode,
        String externalMessageId,
        String rawResponse
) {
}
