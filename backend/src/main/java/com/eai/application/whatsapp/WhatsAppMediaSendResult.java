package com.eai.application.whatsapp;

public record WhatsAppMediaSendResult(
        boolean successful,
        int statusCode,
        String externalMessageId,
        String rawResponse
) {
}
