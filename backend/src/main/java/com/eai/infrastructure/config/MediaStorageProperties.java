package com.eai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eai.media.storage")
public record MediaStorageProperties(
        String provider,
        String localDirectory
) {
    public String effectiveProvider() {
        return provider == null || provider.isBlank() ? "local" : provider.trim();
    }

    public String effectiveLocalDirectory() {
        return localDirectory == null || localDirectory.isBlank() ? ".run-logs/media-storage" : localDirectory.trim();
    }
}
