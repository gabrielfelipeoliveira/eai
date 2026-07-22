package com.eai.infrastructure.config;

import com.eai.application.whatsapp.WhatsAppMediaSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "eai.whatsapp.media")
public record WhatsAppMediaProperties(
        Long maxImageSizeBytes,
        Long maxAudioSizeBytes,
        Long maxDocumentSizeBytes,
        List<String> allowedMimeTypes
) implements WhatsAppMediaSettings {
}
