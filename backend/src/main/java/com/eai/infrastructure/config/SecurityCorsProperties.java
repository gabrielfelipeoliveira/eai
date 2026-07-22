package com.eai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "eai.security.cors")
public record SecurityCorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders
) {
    public List<String> effectiveAllowedOrigins() {
        return requireValues(allowedOrigins, "EAI_CORS_ALLOWED_ORIGINS is required");
    }

    public List<String> effectiveAllowedMethods() {
        return requireValues(allowedMethods, "EAI_CORS_ALLOWED_METHODS is required");
    }

    public List<String> effectiveAllowedHeaders() {
        return requireValues(allowedHeaders, "EAI_CORS_ALLOWED_HEADERS is required");
    }

    private List<String> requireValues(List<String> values, String message) {
        if (values == null || values.isEmpty() || values.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalStateException(message);
        }
        return values;
    }
}
