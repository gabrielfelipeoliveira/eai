package com.eai.application.whatsapp;

import com.eai.application.common.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WhatsAppMediaValidator {

    private static final long DEFAULT_MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final long DEFAULT_MAX_AUDIO_SIZE_BYTES = 16L * 1024 * 1024;
    private static final long DEFAULT_MAX_DOCUMENT_SIZE_BYTES = 100L * 1024 * 1024;
    private static final Set<String> DEFAULT_ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "audio/aac",
            "audio/mp4",
            "audio/mpeg",
            "audio/amr",
            "audio/ogg",
            "text/plain",
            "application/pdf",
            "application/vnd.ms-powerpoint",
            "application/msword",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private final WhatsAppMediaSettings settings;

    public void validateUpload(String mimeType, long sizeBytes) {
        validate(mimeType, sizeBytes);
    }

    public void validateDownload(String mimeType, long sizeBytes) {
        validate(mimeType, sizeBytes);
    }

    public void validateDownload(String mimeType, Long sizeBytes) {
        if (sizeBytes != null) {
            validate(mimeType, sizeBytes);
        }
    }

    private void validate(String mimeType, long sizeBytes) {
        if (sizeBytes <= 0) {
            throw new ApplicationException("WHATSAPP_MEDIA_FILE_EMPTY", "Media file is empty");
        }
        String normalizedMimeType = normalizeMimeType(mimeType);
        if (!allowedMimeTypes().contains(normalizedMimeType)) {
            throw new ApplicationException("WHATSAPP_MEDIA_MIME_TYPE_UNSUPPORTED", "Media MIME type is not supported");
        }
        long maxSizeBytes = maxSizeBytes(normalizedMimeType);
        if (sizeBytes > maxSizeBytes) {
            throw new ApplicationException("WHATSAPP_MEDIA_FILE_TOO_LARGE", "Media file exceeds the configured size limit");
        }
    }

    private Set<String> allowedMimeTypes() {
        if (settings.allowedMimeTypes() == null || settings.allowedMimeTypes().isEmpty()) {
            return DEFAULT_ALLOWED_MIME_TYPES;
        }
        Set<String> configured = settings.allowedMimeTypes().stream()
                .filter(value -> value != null && !value.isBlank())
                .map(this::normalizeMimeType)
                .collect(Collectors.toSet());
        return configured.isEmpty() ? DEFAULT_ALLOWED_MIME_TYPES : configured;
    }

    private long maxSizeBytes(String mimeType) {
        if (mimeType.startsWith("image/")) {
            return configuredOrDefault(settings.maxImageSizeBytes(), DEFAULT_MAX_IMAGE_SIZE_BYTES);
        }
        if (mimeType.startsWith("audio/")) {
            return configuredOrDefault(settings.maxAudioSizeBytes(), DEFAULT_MAX_AUDIO_SIZE_BYTES);
        }
        return configuredOrDefault(settings.maxDocumentSizeBytes(), DEFAULT_MAX_DOCUMENT_SIZE_BYTES);
    }

    private long configuredOrDefault(Long value, long fallback) {
        return value == null || value <= 0 ? fallback : value;
    }

    private String normalizeMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            throw new ApplicationException("WHATSAPP_MEDIA_MIME_TYPE_UNSUPPORTED", "Media MIME type is not supported");
        }
        return mimeType.trim().toLowerCase(Locale.ROOT);
    }
}
