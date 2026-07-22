package com.eai.application.whatsapp;

public record WhatsAppMediaUploadResult(
        boolean successful,
        int statusCode,
        String mediaId,
        String rawResponse
) {
}
