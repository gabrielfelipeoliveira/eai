package com.eai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "eai.email.credentials")
public record EmailCredentialEncryptionProperties(
        String secret,
        List<String> previousSecrets,
        Boolean reencryptOnStartup
) {
    public String effectiveSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("EAI_EMAIL_CREDENTIALS_SECRET is required");
        }
        return secret.trim();
    }

    public List<String> effectivePreviousSecrets() {
        if (previousSecrets == null || previousSecrets.isEmpty()) {
            return List.of();
        }
        return previousSecrets.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .filter(value -> !value.equals(effectiveSecret()))
                .distinct()
                .toList();
    }

    public boolean reencryptOnStartupEnabled() {
        return Boolean.TRUE.equals(reencryptOnStartup);
    }
}
