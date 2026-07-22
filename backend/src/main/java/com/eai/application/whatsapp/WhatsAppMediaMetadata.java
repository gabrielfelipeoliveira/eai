package com.eai.application.whatsapp;

public record WhatsAppMediaMetadata(
        String mediaId,
        String url,
        String mimeType,
        Long fileSizeBytes,
        String sha256,
        String rawResponse
) {
}
