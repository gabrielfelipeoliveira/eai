package com.eai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eai.security.refresh-cookie")
public record SecurityRefreshCookieProperties(
        String name,
        Boolean secure,
        String sameSite
) {
    public String effectiveName() {
        return name == null || name.isBlank() ? "eai.refreshToken" : name.trim();
    }

    public boolean effectiveSecure() {
        return Boolean.TRUE.equals(secure);
    }

    public String effectiveSameSite() {
        return sameSite == null || sameSite.isBlank() ? "Strict" : sameSite.trim();
    }
}
