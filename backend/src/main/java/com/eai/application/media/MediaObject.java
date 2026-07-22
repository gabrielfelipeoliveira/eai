package com.eai.application.media;

public record MediaObject(
        String provider,
        String key,
        String fileName,
        String mimeType,
        long sizeBytes,
        String sha256,
        byte[] content
) {
}
