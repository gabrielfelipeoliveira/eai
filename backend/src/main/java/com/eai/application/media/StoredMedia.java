package com.eai.application.media;

public record StoredMedia(
        String provider,
        String key,
        String fileName,
        String mimeType,
        long sizeBytes,
        String sha256
) {
}
