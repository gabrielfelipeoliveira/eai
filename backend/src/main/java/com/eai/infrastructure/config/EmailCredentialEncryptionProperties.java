package com.eai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eai.email.credentials")
public record EmailCredentialEncryptionProperties(
        String secret
) {
    public String effectiveSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("EAI_EMAIL_CREDENTIALS_SECRET is required");
        }
        return secret;
    }
}
