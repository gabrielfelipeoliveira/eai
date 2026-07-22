package com.eai.application.whatsapp;

public record WhatsAppMediaDownload(
        byte[] content,
        String rawResponse
) {
}
